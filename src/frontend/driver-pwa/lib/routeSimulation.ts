/**
 * Route Simulation Utilities
 *
 * This module provides utilities for simulating a driver's trip along a route.
 * It animates the position of a car marker along the route geometry.
 */

export interface SimulationState {
  isRunning: boolean;
  isPaused: boolean;
  progress: number; // 0-100
  currentPosition: [number, number];
  speed: number; // 1x, 2x, 5x, 10x
}

export type SimulationCallback = (position: [number, number], progress: number, currentIndex: number) => void;

export class RouteSimulator {
  private route: [number, number][] = [];
  private isRunning = false;
  private isPaused = false;
  private currentIndex = 0;
  private speed = 1; // 1x speed
  private animationFrameId: number | null = null;
  private lastUpdateTime = 0;
  private callback: SimulationCallback | null = null;

  // Configuration
  private readonly baseUpdateInterval = 100; // milliseconds between updates at 1x speed
  private readonly stepsPerUpdate = 1; // number of route points to advance per update

  constructor(routeGeometry: [number, number][]) {
    this.route = routeGeometry;
  }

  /**
   * Set the callback function to be called when position updates
   */
  setCallback(callback: SimulationCallback): void {
    this.callback = callback;
  }

  /**
   * Set simulation speed (1x, 2x, 5x, 10x)
   */
  setSpeed(speed: number): void {
    this.speed = Math.max(0.5, Math.min(20, speed)); // Clamp between 0.5x and 20x
  }

  /**
   * Start the simulation
   */
  start(): void {
    if (this.isRunning && !this.isPaused) return;

    this.isRunning = true;
    this.isPaused = false;
    this.lastUpdateTime = Date.now();
    this.animate();
  }

  /**
   * Pause the simulation
   */
  pause(): void {
    this.isPaused = true;
  }

  /**
   * Resume the simulation
   */
  resume(): void {
    if (!this.isRunning) {
      this.start();
      return;
    }

    this.isPaused = false;
    this.lastUpdateTime = Date.now();
    this.animate();
  }

  /**
   * Stop and reset the simulation
   */
  stop(): void {
    this.isRunning = false;
    this.isPaused = false;
    this.currentIndex = 0;

    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }

    // Reset to start position
    if (this.route.length > 0 && this.callback) {
      this.callback(this.route[0], 0, 0);
    }
  }

  /**
   * Get current simulation state
   */
  getState(): SimulationState {
    const progress = this.route.length > 0
      ? (this.currentIndex / (this.route.length - 1)) * 100
      : 0;

    const currentPosition = this.route[this.currentIndex] || this.route[0];

    return {
      isRunning: this.isRunning,
      isPaused: this.isPaused,
      progress: Math.min(100, progress),
      currentPosition,
      speed: this.speed,
    };
  }

  /**
   * Check if simulation has finished
   */
  isFinished(): boolean {
    return this.currentIndex >= this.route.length - 1;
  }

  /**
   * Animation loop
   */
  private animate = (): void => {
    if (!this.isRunning || this.isPaused) return;

    const now = Date.now();
    const elapsed = now - this.lastUpdateTime;
    const updateInterval = this.baseUpdateInterval / this.speed;

    if (elapsed >= updateInterval) {
      this.updatePosition();
      this.lastUpdateTime = now;
    }

    if (this.currentIndex < this.route.length - 1) {
      this.animationFrameId = requestAnimationFrame(this.animate);
    } else {
      // Reached the end
      this.isRunning = false;
      if (this.callback) {
        this.callback(this.route[this.route.length - 1], 100, this.route.length - 1);
      }
    }
  };

  /**
   * Update the current position along the route
   */
  private updatePosition(): void {
    if (this.currentIndex >= this.route.length - 1) {
      return;
    }

    // Advance along the route
    this.currentIndex += this.stepsPerUpdate;

    // Clamp to route bounds
    this.currentIndex = Math.min(this.currentIndex, this.route.length - 1);

    const currentPosition = this.route[this.currentIndex];
    const progress = (this.currentIndex / (this.route.length - 1)) * 100;

    if (this.callback) {
      this.callback(currentPosition, progress, this.currentIndex);
    }
  }
}
