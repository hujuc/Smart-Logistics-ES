#!/usr/bin/env python3
import time
import subprocess
import sys
import os
from datetime import datetime

# Configuration
SERVICES = {
    "backend": 2,
    "admin-console": 2,
    "customer-portal": 2,
    "driver-pwa": 2,
    "landing-page": 2
}

# Mapping service names to container name prefixes
CONTAINER_PREFIXES = {
    "backend": "slms-backend-prod",
    "admin-console": "slms-admin-console-prod",
    "customer-portal": "slms-customer-portal-prod",
    "driver-pwa": "slms-driver-pwa-prod",
    "landing-page": "slms-landing-page-prod"
}

TERRAFORM_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "terraform")

def log(message):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {message}")

def run_command(command, cwd=None):
    try:
        result = subprocess.run(
            command, 
            shell=True, 
            check=True, 
            stdout=subprocess.PIPE, 
            stderr=subprocess.PIPE,
            cwd=cwd,
            text=True
        )
        return True, result.stdout.strip()
    except subprocess.CalledProcessError as e:
        return False, e.stderr.strip()

def check_container_status(container_name):
    """
    Returns: 'running', 'stopped', 'missing'
    """
    success, output = run_command(f"docker inspect -f '{{{{.State.Status}}}}' {container_name}")
    if not success:
        if "No such object" in output:
            return "missing"
        return "error"
    return output.strip()

def resurrect_container(container_name):
    log(f"Resurrecting stopped container: {container_name}")
    success, output = run_command(f"docker start {container_name}")
    if success:
        log(f"Successfully started {container_name}")
    else:
        log(f"Failed to start {container_name}: {output}")

def recreate_missing_containers():
    log("Detected missing containers. Triggering Terraform apply to recreate them...")
    success, output = run_command("terraform apply -auto-approve", cwd=TERRAFORM_DIR)
    if success:
        log("Terraform apply completed successfully.")
    else:
        log(f"Terraform apply failed: {output}")

def monitor_loop():
    log("Starting SLMS Production Container Monitor...")
    log(f"Monitoring {len(SERVICES)} services with 2 replicas each.")
    
    while True:
        missing_containers_found = False
        
        for service, count in SERVICES.items():
            prefix = CONTAINER_PREFIXES[service]
            
            for i in range(1, count + 1):
                container_name = f"{prefix}-{i}"
                status = check_container_status(container_name)
                
                if status == "running":
                    # All good
                    pass
                elif status == "exited" or status == "created":
                    log(f"ALERT: {container_name} is {status}!")
                    resurrect_container(container_name)
                elif status == "missing":
                    log(f"CRITICAL: {container_name} is MISSING!")
                    missing_containers_found = True
                else:
                    log(f"WARNING: {container_name} status is {status}")
        
        if missing_containers_found:
            recreate_missing_containers()
            
        time.sleep(10)

if __name__ == "__main__":
    try:
        monitor_loop()
    except KeyboardInterrupt:
        log("Monitor stopped by user.")
        sys.exit(0)
