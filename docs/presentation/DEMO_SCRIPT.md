# SLMS Live Demo Script

**Total Duration:** 7 minutes
**Demo Operator:** Usually QA Engineer or most technically confident team member
**Backup Operator:** Designate a backup in case primary can't present

---

## Pre-Demo Setup Checklist (15 minutes before presentation)

### 1. System Startup
```bash
# Start all services
cd /home/roldao/Desktop/MEI/ES/group-project-es2526_304
make up  # or make dev-up for faster startup

# Verify all services are healthy
make status

# Check logs for any errors
make logs
```

**Verify Services Running:**
- [ ] Backend (Spring Boot) - http://localhost:8080
- [ ] Admin Console - http://localhost:3001
- [ ] Customer Portal - http://localhost:3002
- [ ] Driver PWA - http://localhost:3003
- [ ] Landing Page - http://localhost:3000
- [ ] Keycloak - http://localhost:8180
- [ ] PostgreSQL - healthy
- [ ] MinIO - http://localhost:9000
- [ ] Kafka - healthy

### 2. Database Seeding

**Ensure Test Data Exists:**
- [ ] At least 2-3 pending orders in warehouse queue
- [ ] At least 1 order in "ready for carrier" status
- [ ] At least 1 order assigned to driver for delivery
- [ ] At least 1-2 delivery exceptions in CSR queue
- [ ] Carrier integrations configured (ShipEngine/Shippo API keys valid)
- [ ] AI chatbot documents seeded (check Pinecone has embeddings)

**Test Order Details to Use:**
```
Order ID: [Note actual ID from your DB]
Tracking Number: TRK-XXXXXXXXXX [Note from your DB]
Customer Name: John Silva
Destination: Rua da Estação, 123, Aveiro, 3810-164, Portugal
Package: Laptop Computer (3kg, 40x30x10cm)
```

### 3. Browser Setup

**Open 4 Browser Windows (or tabs with clear labels):**

**Window 1: Admin Console - Warehouse Staff**
- URL: http://localhost:3001
- Login: warehouse_staff / password (or your test credentials)
- Navigate to: Orders → Pending Orders page
- Keep this page loaded and ready

**Window 2: Driver PWA**
- URL: http://localhost:3003
- Login: driver_user / password (or your test credentials)
- Navigate to: Delivery Manifest page
- Keep this page loaded

**Window 3: Customer Portal**
- URL: http://localhost:3002
- Login: customer_user / password (or your test credentials)
- Navigate to: Order Tracking page
- Keep this page loaded

**Window 4: Admin Console - CSR / Manager**
- URL: http://localhost:3001
- Login: logistics_manager / password OR customer_service / password
- Navigate to: Dashboard (for manager) or Exceptions (for CSR)
- Keep this page loaded

**Pro Tip:** Use browser profiles or incognito windows to avoid session conflicts

### 4. Demo Environment Test

**Test Each Flow Once:**
- [ ] Can create/view order in Admin Console
- [ ] Carrier rate API returns results (not error 500)
- [ ] Can generate shipping label (MinIO accessible)
- [ ] Driver app loads manifest correctly
- [ ] Customer portal shows order tracking
- [ ] Chatbot responds to test query in any frontend
- [ ] Network tab shows API calls clearly

### 5. Network Tab Preparation

**Open Browser Developer Tools:**
- Press F12 or Cmd+Option+I (Mac)
- Go to **Network** tab
- **Filter:** Set filter to "Fetch/XHR" to show API calls only
- **Clear:** Clear network log before demo starts
- **Preserve log:** Enable "Preserve log" so entries don't disappear on navigation

**Why:** You'll use this to show AI chatbot tool execution in real-time

### 6. Backup Materials Ready

**Prepare Backup Screenshots (in case live demo fails):**
- Screenshot of each major screen (orders dashboard, manifest, tracking)
- Screenshot of carrier rate comparison results
- Screenshot of generated shipping label
- Screenshot of chatbot conversation with tool execution
- Screenshot of KPI dashboard

**Save screenshots in:** `docs/presentation/demo-screenshots/`

**Optional:** Record a 2-3 minute backup video demo

---

## Demo Flow: Step-by-Step Execution

### Introduction (10 seconds)
**Say:**
> "Alright, let's see SLMS in action. I'm going to walk you through a complete order lifecycle - from warehouse to doorstep. I have four browser windows open, each logged in as a different user."

**Action:** Briefly show all 4 windows side by side or quickly tab through them

---

## Part 1: Warehouse Operations (1 minute 30 seconds)

### Step 1.1: Show Orders Dashboard (15 seconds)

**Window:** Admin Console (Warehouse Staff)

**Say:**
> "This is the Admin Console. I'm logged in as warehouse staff. Here's my orders dashboard showing all pending orders that need processing."

**What to Point Out:**
- Total order count
- Status filters (Pending, Ready, Assigned, etc.)
- Order list with customer names and destinations

**Action:** Hover over or click on different sections to show interactivity

---

### Step 1.2: Select Order for Processing (15 seconds)

**Say:**
> "Let me select this order for John Silva - a laptop being shipped to Aveiro."

**Action:**
1. Click on a specific order in the list
2. Order detail page opens

**What to Point Out:**
- Customer information
- Destination address
- Package details (weight, dimensions)
- Current status: "Ready for Carrier Assignment"

**Say:**
> "The order is created and validated. Now I need to choose a carrier."

---

### Step 1.3: Request Carrier Rates (30 seconds)

**Say:**
> "I'll request carrier rates. Watch as the system queries FedEx, UPS, and DHL through the ShipEngine API..."

**Action:**
1. Click "Compare Carriers" or "Get Rates" button
2. **Wait for loading spinner** (2-3 seconds)
3. Rate comparison table appears

**What to Point Out (when results appear):**
> "There we go. I can see:"
- FedEx Express: $24.50, 1-2 days
- UPS Ground: $18.75, 2-3 days
- DHL International: $32.00, 1 day

**Say:**
> "UPS offers the best balance of cost and speed for this shipment. I'll select UPS."

**Action:**
1. Click "Select" next to UPS Ground
2. Confirmation appears: "Carrier assigned successfully"

**Fallback if API fails:**
> "The carrier API would return rates here. In testing, we've confirmed FedEx, UPS, and DHL integration works. Let me proceed with a pre-selected carrier..."

---

### Step 1.4: Generate Shipping Label (20 seconds)

**Say:**
> "Now I'll generate the shipping label. The system calls the UPS API, retrieves the PDF, and stores it in MinIO for download..."

**Action:**
1. Click "Generate Label" button
2. **Wait for processing** (3-5 seconds)
3. Label generation success message + Download button appears

**Say:**
> "Perfect. Here's the label ready for printing."

**Action:**
1. Click "Download Label" or "View Label"
2. PDF opens in new tab or modal

**What to Point Out:**
- Tracking barcode
- Sender and recipient addresses
- UPS branding and service level
- Package weight and dimensions

**Say:**
> "This label has everything the carrier needs: tracking barcode, addresses, service level. The warehouse staff would print this and attach it to the package."

**Action:** Close PDF preview

---

### Step 1.5: Confirm Carrier Pickup (10 seconds)

**Say:**
> "When the UPS driver arrives to collect packages, I confirm the pickup..."

**Action:**
1. Click "Confirm Pickup" button
2. Status changes to "Picked Up by Carrier"
3. Success notification: "Pickup confirmed. Customer notified."

**What to Point Out:**
> "The order status updates immediately, and the system sends a notification to the customer letting them know their package is on the way."

---

## Part 2: Driver Delivery (1 minute 30 seconds)

### Step 2.1: Switch to Driver PWA (5 seconds)

**Say:**
> "Now let's switch perspectives. I'm now a delivery driver using the mobile PWA."

**Action:**
1. Switch to Driver PWA browser window
2. Briefly show the interface

**Say:**
> "This app is optimized for mobile devices and works offline if the driver loses connectivity."

---

### Step 2.2: View Delivery Manifest (20 seconds)

**Say:**
> "Here's my delivery manifest for today."

**What to Point Out:**
- Total number of stops (e.g., "8 deliveries scheduled")
- List of deliveries with addresses
- Priority indicators or delivery windows (if visible)
- Map view showing all stops (if integrated)

**Say:**
> "I can see all my deliveries for the day. Let me select the order we just processed - the laptop for John Silva."

**Action:**
1. Click on the specific order in the manifest
2. Order detail page opens

---

### Step 2.3: View Order Details & Navigation (30 seconds)

**What to Show:**
- Delivery address: Rua da Estação, 123, Aveiro
- Customer contact: Phone number, email
- Package details: Laptop, 3kg
- Special instructions (if any)
- Navigation button

**Say:**
> "I can see the destination address, customer contact info, and package details."

**Action (if Mapbox is integrated):**
1. Click "Navigate" or "Get Directions"
2. Map appears with route from current location to destination

**Say:**
> "The app provides turn-by-turn navigation using Mapbox. In the field, drivers would follow these directions to the customer's location."

**If Navigation Not Integrated:**
> "In production, this would show turn-by-turn navigation. For this demo, I'll move directly to proof of delivery."

---

### Step 2.4: Capture Proof of Delivery (30 seconds)

**Say:**
> "When I arrive at the customer's location, I capture proof of delivery."

**Action:**
1. Scroll to "Proof of Delivery" section
2. Show upload options:
   - Photo upload button
   - Signature pad (if integrated)
   - Notes field

**Say:**
> "I can take a photo of the delivered package, capture the customer's signature, and add any notes."

**Action:**
1. Click "Upload Photo"
2. Select a pre-prepared test image (e.g., package on doorstep)
   - **Prepare image beforehand:** Have a sample "delivered package" photo ready
3. Image preview appears

**Say:**
> "Photo uploaded. Now I add a note..."

**Action:**
1. Type in notes field: "Delivered to customer. Package in good condition."

---

### Step 2.5: Mark as Delivered (5 seconds)

**Say:**
> "And I'll mark this delivery as completed..."

**Action:**
1. Click "Mark as Delivered" button
2. Confirmation modal may appear: "Are you sure?"
3. Click "Confirm"
4. Success message: "Delivery completed successfully"
5. Status changes to "Delivered"

**Say:**
> "Done. The order status updates in real-time across the entire system. Let's see what the customer experiences..."

---

## Part 3: Customer Tracking (1 minute)

### Step 3.1: Switch to Customer Portal (5 seconds)

**Say:**
> "Now I'm viewing the Customer Portal - what John Silva sees when tracking his order."

**Action:**
1. Switch to Customer Portal browser window
2. Show the interface

---

### Step 3.2: Order Tracking (25 seconds)

**Say:**
> "The customer can search for their order by tracking number."

**Action:**
1. If not already showing the order, use search/tracking input
2. Enter tracking number: TRK-XXXXXXXXXX
3. Click "Track Order" or press Enter
4. Order details page loads

**What to Point Out:**
- Order status: "Delivered"
- Delivery timestamp: "Today at 2:15 PM"
- Complete event timeline

**Say:**
> "Here's the complete order history with timestamps:"

**Read Timeline Events (point to each):**
- "Order created - [timestamp]"
- "Label generated - [timestamp]"
- "Picked up by carrier - [timestamp]"
- "Out for delivery - [timestamp]"
- "Delivered - [timestamp]"

---

### Step 3.3: View Proof of Delivery (15 seconds)

**Say:**
> "And here's the proof of delivery photo the driver just uploaded."

**What to Point Out:**
- POD photo displayed
- Driver notes visible
- Delivery completion details

**Say:**
> "Complete transparency for the customer. They know exactly when their package was delivered and can see photographic proof."

---

### Step 3.4: Chatbot Introduction (15 seconds)

**Say:**
> "Now, if the customer has questions, they don't need to call support. They can use our AI-powered chatbot."

**Action:**
1. Look for chatbot widget (usually bottom-right corner)
2. Click to open chatbot interface

**Say:**
> "This is where it gets interesting. Let me show you what makes our chatbot special..."

**Transition:**
> "Our chatbot isn't just an FAQ bot. It uses Retrieval-Augmented Generation and can execute database queries in real-time with proper authorization. Let me demonstrate."

---

## Part 4: AI Chatbot - THE STAR FEATURE (2-3 minutes)

### Step 4.1: Setup for Demo (10 seconds)

**IMPORTANT PREPARATION:**
1. **Open Browser Developer Tools** (F12)
2. Go to **Network** tab
3. Set filter to **Fetch/XHR**
4. **Clear** network log
5. Position window so audience can see both chatbot and network tab

**Say:**
> "I'm opening the browser's network tab so you can see what happens behind the scenes when the chatbot executes tools."

---

### Step 4.2: First Query - Tool Execution (45 seconds)

**Say:**
> "Let me ask the chatbot: 'What's the status of my order TRK-XXXXXXXXXX?'"

**Action:**
1. Type in chatbot input: `What's the status of order TRK-XXXXXXXXXX?`
   - **Use the actual tracking number from your test order**
2. Press Send or Enter
3. **Watch Network Tab**

**What to Point Out (during response):**
1. **Network Tab Activity:**
   - Request to `/api/chatbot/chat` or similar endpoint
   - Response shows tool execution details (if logged)

2. **Chatbot Response Area:**
   - Typing indicator appears
   - Response streams in

**Expected Response Example:**
> "Your order (Laptop Computer for John Silva) was delivered today at 2:15 PM to Rua da Estação, 123, Aveiro. The driver captured proof of delivery with a photo and noted: 'Delivered to customer. Package in good condition.'"

**Say:**
> "Notice what just happened. The chatbot:"
- "Understood my natural language question"
- "Determined it needed real-time data from the database"
- "Selected the OrderStatusTool from its available tools"
- "Executed the tool with the tracking number I provided"
- "Retrieved current order information from PostgreSQL"
- "Formatted that data into a natural language response"

**Emphasize:**
> "This is live database data, not cached or pre-scripted. The chatbot accessed the real order we just delivered."

---

### Step 4.3: Show Backend Logs (Optional - 15 seconds)

**If Time Allows and Setup:**

**Say:**
> "Let me quickly show you the backend logs to prove this tool execution."

**Action:**
1. Switch to terminal window with backend logs running
2. Scroll to recent logs
3. Look for lines like:
   - `Executing tool: OrderStatusTool`
   - `Tool parameters: {trackingNumber: "TRK-XXXXXXXXXX"}`
   - `Tool execution successful`

**Say:**
> "You can see the backend log showing tool selection, execution, and success. This is real-time database access."

**Switch back to chatbot interface**

---

### Step 4.4: Second Query - RAG Demonstration (40 seconds)

**Say:**
> "Now let me ask a different type of question. This one doesn't need database access - it's about how to use the system."

**Action:**
1. Type in chatbot: `How do I update my delivery address?`
2. Press Send

**What to Point Out:**
> "For this question, the chatbot uses a different approach called RAG - Retrieval-Augmented Generation."

**While waiting for response:**
> "Here's what's happening:"
- "My question is converted to a semantic vector - a 768-dimensional representation of meaning"
- "That vector is sent to Pinecone, our vector database"
- "Pinecone searches for semantically similar help documentation"
- "The most relevant documents are retrieved and injected as context"
- "Gemini generates a response grounded in that documentation"

**Expected Response Example:**
> "To update your delivery address: 1) Go to Account Settings from the menu, 2) Click 'Delivery Addresses', 3) Edit your primary address or add a new one, 4) Save changes. Note: You can only update the address before the package is out for delivery."

**Say:**
> "Notice the response is specific, accurate, and based on our actual documentation - not hallucinated by the AI."

---

### Step 4.5: Third Query - Role-Based Authorization (45 seconds)

**Say:**
> "Now here's the security aspect. Let me switch to the Admin Console and ask a privileged question."

**Action:**
1. Switch to Admin Console browser window
2. Open chatbot (should be logged in as warehouse staff or manager)
3. Clear any previous chat history (if possible)

**Say:**
> "I'm now logged in as a warehouse staff member. I'll ask: 'Show me all pending orders.'"

**Action:**
1. Type: `Show me all pending orders`
2. Press Send

**Expected Response Example:**
> "You have 5 pending orders:
> 1. ORD-2024-0123 - João Santos - Porto - 2.5kg - Created 3 hours ago
> 2. ORD-2024-0124 - Maria Oliveira - Lisboa - 1.2kg - Created 2 hours ago
> [...]"

**Say:**
> "The chatbot executed a database query and returned operational data. But here's the key: **if a customer asked this same question, they would be denied access.**"

**Action (if time allows):**
1. Switch back to Customer Portal chatbot
2. Ask the same question: `Show me all pending orders`

**Expected Response Example:**
> "I'm sorry, but I can only provide information about your own orders. I don't have access to show you all pending orders in the system. Would you like to track one of your orders instead?"

**Say:**
> "See that? Tool execution enforces role-based authorization. The chatbot knows who's asking and what they're allowed to see."

**Emphasize the Innovation:**
> "This combination of RAG for documentation and tool calling for real-time data - with proper authorization - is what makes our chatbot unique. It's not just answering questions from static FAQs. It's a context-aware, role-specific assistant that can access live operational data securely."

---

### Step 4.6: Chatbot Summary (10 seconds)

**Say:**
> "So the chatbot provides:"
- "Natural language understanding using Google Gemini"
- "Semantic search over documentation using Pinecone vectors"
- "Real-time database access through authorized tools"
- "Role-specific responses based on user permissions"

**Say:**
> "This is production-ready AI assistance, not a prototype."

---

## Part 5: Exception Management (1 minute - OPTIONAL)

**NOTE:** Skip this section if running short on time. It's not essential.

### Step 5.1: Switch to Admin Console - CSR View (5 seconds)

**Say:**
> "Quick look at exception handling. When deliveries fail, problems are tracked and resolved systematically."

**Action:**
1. Switch to Admin Console (logged in as CSR or use the manager account)
2. Navigate to "Exceptions" or "Delivery Issues" page

---

### Step 5.2: View Exception Queue (25 seconds)

**What to Point Out:**
- Total active exceptions
- Priority levels (Critical, High, Medium, Low)
- Exception types (Recipient absent, Wrong address, Damaged package, etc.)
- SLA indicators (time remaining to resolve)

**Say:**
> "Here's the exception queue. Each delivery problem is logged, prioritized, and assigned to a CSR."

**Action:**
1. Click on a specific exception
2. Exception detail page opens

**What to Show:**
- Problem description: "Recipient absent during delivery attempt"
- Order details (tracking number, customer info)
- When exception was created
- SLA deadline: "Must resolve within 4 hours"
- Current assignee
- Resolution notes field

---

### Step 5.3: Resolve Exception (20 seconds)

**Say:**
> "The CSR contacts the customer, arranges redelivery, and documents the resolution."

**Action:**
1. Scroll to "Resolution" section
2. Type in notes field: "Contacted customer. Redelivery scheduled for tomorrow, 2-4 PM."
3. Change status dropdown to "Resolved"
4. Click "Save" or "Resolve Exception"

**Say:**
> "The exception is marked resolved, the customer is notified, and the delivery is rescheduled. This ensures nothing falls through the cracks."

---

## Part 6: Manager Dashboard (30 seconds - OPTIONAL)

**NOTE:** Skip if running short on time.

### Step 6.1: Switch to Manager View (5 seconds)

**Say:**
> "Finally, a quick look at the manager dashboard."

**Action:**
1. Navigate to Dashboard or Analytics page (as Logistics Manager)

---

### Step 6.2: Show KPI Dashboard (25 seconds)

**What to Point Out:**
- **Real-time metrics cards:**
  - Orders Today: 47
  - In Transit: 23
  - Delivered Today: 18
  - On-Time Delivery Rate: 94%
  - Average Cost per Shipment: €12.50
  - Active Exceptions: 3

**Say:**
> "Logistics managers see real-time KPIs: order volumes, in-transit shipments, on-time delivery rates, costs, and exception counts."

**Point to Carrier Performance Chart (if visible):**
> "They can compare carrier performance over time - which carrier is most reliable, which has the best rates, where exceptions are coming from."

**Say:**
> "This data-driven visibility lets managers optimize carrier selection and negotiate better contracts."

---

## Demo Conclusion (10 seconds)

**Say:**
> "And that's SLMS in action - a complete journey from warehouse to customer, with AI-powered assistance and real-time visibility throughout. Every part of this is fully functional, tested, and monitored."

**Action:**
1. Close browser windows or minimize
2. Return to presentation slides

**Transition Back to Slides:**
> "Now let me explain the engineering practices that made building this system possible..."

---

## Timing Breakdown

| Section | Duration | Cumulative |
|---------|----------|------------|
| Introduction | 0:10 | 0:10 |
| **Part 1: Warehouse** | **1:30** | **1:40** |
| - Orders Dashboard | 0:15 | - |
| - Select Order | 0:15 | - |
| - Carrier Rates | 0:30 | - |
| - Generate Label | 0:20 | - |
| - Confirm Pickup | 0:10 | - |
| **Part 2: Driver** | **1:30** | **3:10** |
| - Switch to Driver PWA | 0:05 | - |
| - View Manifest | 0:20 | - |
| - Order Details & Nav | 0:30 | - |
| - Capture POD | 0:30 | - |
| - Mark Delivered | 0:05 | - |
| **Part 3: Customer** | **1:00** | **4:10** |
| - Switch to Portal | 0:05 | - |
| - Order Tracking | 0:25 | - |
| - View POD | 0:15 | - |
| - Chatbot Intro | 0:15 | - |
| **Part 4: AI Chatbot** | **2:30** | **6:40** |
| - Setup Network Tab | 0:10 | - |
| - Query 1: Tool Execution | 0:45 | - |
| - Backend Logs (optional) | 0:15 | - |
| - Query 2: RAG Demo | 0:40 | - |
| - Query 3: Authorization | 0:45 | - |
| - Summary | 0:10 | - |
| **Part 5: Exceptions** | **1:00** | **7:40** |
| (OPTIONAL - Skip if needed) | | |
| **Part 6: Dashboard** | **0:30** | **8:10** |
| (OPTIONAL - Skip if needed) | | |
| **Conclusion** | **0:10** | **7:00-8:20** |

**Target Duration:** 7 minutes (includes Parts 1-4 only)
**Maximum Duration:** 8 minutes (includes all optional parts)

---

## Troubleshooting & Fallback Plans

### If Carrier API Fails (Rate Comparison)

**Symptom:** Clicking "Get Rates" returns error or times out

**Fallback:**
> "The carrier API would return rates here. Let me explain what normally happens: the system queries FedEx, UPS, and DHL through ShipEngine, retrieves quotes in 2-3 seconds, and displays them in a comparison table. In our testing, we've confirmed this integration works reliably. For the demo, I'll proceed with a pre-selected carrier."

**Action:** Manually select a carrier and continue

---

### If Label Generation Fails

**Symptom:** "Generate Label" button errors or hangs

**Fallback:**
> "Label generation is calling the carrier API to create the PDF. In testing, this works consistently. Let me show you a previously generated label..."

**Action:**
1. Open a pre-saved label PDF from `docs/presentation/demo-screenshots/sample-label.pdf`
2. Show the label format
3. Continue demo

---

### If Chatbot Doesn't Respond

**Symptom:** Chatbot times out or returns error

**Fallback:**
> "The chatbot is attempting to query Gemini and Pinecone. Network latency can occasionally cause delays. Let me show you a screenshot of a successful interaction..."

**Action:**
1. Open screenshot showing chatbot conversation
2. Explain what would have happened
3. If possible, show backend logs demonstrating tool execution in past tests

---

### If Database is Empty

**Symptom:** No orders appear, manifests are empty

**Fallback:**
> "It looks like our test data didn't seed correctly. Let me show you screenshots from our testing environment where we've validated all these workflows..."

**Action:**
1. Switch to presentation slides
2. Advance to backup slides with screenshots
3. Walk through the flow using images

---

### If Browser Crashes

**Symptom:** Browser window closes or freezes

**Fallback:**
> "Technical difficulties happen. Good thing we have backups. Let me show you our pre-recorded demo walkthrough..."

**Action:**
1. Play backup video (if prepared)
2. OR use screenshots and narrate

---

### If Internet is Down (External APIs unreachable)

**Symptom:** Carrier APIs, Gemini, Pinecone all fail

**Fallback:**
> "We're experiencing network connectivity issues affecting external service integrations. Let me walk you through the system using our test screenshots, which show the fully functional system."

**Action:** Switch entirely to screenshot-based demo

---

## Demo Best Practices

### Before You Start
- [ ] Take a deep breath
- [ ] Speak clearly and project your voice
- [ ] Check that projector resolution is correct (sometimes windows are cut off)
- [ ] Have water nearby

### During Demo
- **Talk while clicking:** Narrate every action before/during clicking
- **Pause for loading:** If something takes 2-3 seconds to load, fill the silence with explanation
- **Point to the screen:** Use cursor or hand gestures to direct attention
- **Use "watch this":** Before key actions, say "watch what happens when I..."
- **Slow down:** You know the system - audience is seeing it first time
- **Emphasize key moments:** AI tool execution, real-time updates, authorization denials

### After Demo
- **Briefly recap:** "So we've seen order creation, delivery, tracking, and AI assistance"
- **Transition smoothly:** "Let me explain how we built this with professional practices..."
- **Stay calm:** If something broke, don't panic - use your fallback and continue

---

## Emergency Contact (During Presentation)

**If Live Demo Fails Completely:**
- Switch to backup screenshots immediately
- Acknowledge the issue briefly: "Technical issues happen in live demos"
- Narrate through screenshots confidently
- Don't spend time debugging on stage
- Emphasize that the system works in testing (show test reports if needed)

---

## Post-Demo Checklist

After presentation is complete:
- [ ] Close all browser windows
- [ ] Stop Docker services if needed: `make down`
- [ ] Gather feedback on what worked/didn't work
- [ ] Note any issues for future demos
- [ ] Celebrate - you did it! 🎉

---

## Final Tips

**The chatbot demo is your wow factor.** Spend time on it. Practice until you can confidently show:
1. Natural language query
2. Tool execution (visible in network tab or logs)
3. RAG documentation retrieval
4. Role-based authorization

**If you nail the chatbot demo, the presentation will be memorable.**

**Remember:** You built something impressive. Own it. Show it with pride.

Good luck! 🚀
