# Personas, Scenarios & User Stories

## Personas

<img alt="alt text" src="imgs/joaoFerreira.png" width="150"/>

### João Ferreira - Logistics Manager
- **Age:** 42  
- **Profile:** Senior manager in a retail company, with extensive experience in operations and transportation.  

**Goals**  
- Gain a global view of transportation operations.  
- Reduce shipping costs and increase on-time delivery rates.  
- Evaluate carrier performance.  

**Frustrations**  
- Lack of real-time data.  
- Needing to manually compare different carriers.  

**How he uses SLMS**  
- Monitors KPIs (costs, delivery performance).  
- Manages shipping optimization strategies.  
- Exports reports for performance analysis.  

---

<img alt="alt text" src="imgs/anaMartins.png" width="150"/>

### Ana Martins - Warehouse Staff (Shipping Department)
- **Age:** 29  
- **Profile:** Warehouse employee responsible for preparing and dispatching orders.  

**Goals**  
- Quickly process incoming orders.  
- Ensure each shipment has the correct labels and documentation.  
- Reduce errors in the shipping process.  

**Frustrations**  
- Time-consuming manual processes.  
- Having to deal with different systems depending on the carrier.  

**How she uses SLMS**  
- Registers new orders in the system.  
- Generates labels and shipping documents.  
- Updates the system when the carrier picks up an order.  

---

<img alt="alt text" src="imgs/ritaLopes.png" width="150"/>

### Rita Lopes - Customer Service Representative
- **Age:** 34  
- **Profile:** Call center staff member, answering customer calls and emails about deliveries.  

**Goals**  
- Quickly access order status.  
- Resolve delivery issues such as delays or failed attempts.  
- Improve the customer service experience.  

**Frustrations**  
- Not having real-time information.  
- Having to manually contact carriers for updates.  

**How she uses SLMS**  
- Checks order statuses.  
- Provides immediate feedback to customers.  
- Records and follows up on delivery exceptions.  

---

<img alt="alt text" src="imgs/carlosSilva.png" width="150"/>

### Carlos Silva - Driver/Carrier
- **Age:** 38  
- **Profile:** Carrier driver responsible for urban and intercity delivery routes.  

**Goals**  
- Receive clear delivery and route instructions.  
- Deliver packages quickly and efficiently.  
- Easily record proof of delivery.  

**Frustrations**  
- Wasting time due to poorly planned routes.  
- Difficulty updating delivery status on the spot.  

**How he uses SLMS**  
- Views his delivery manifest via a mobile app.  
- Receives optimized routes.  
- Updates delivery statuses (including photo/signature as proof).  

---

<img alt="alt text" src="imgs/marianaCosta.png" width="150"/>

### Mariana Costa - Customer
- **Age:** 26  
- **Profile:** Frequent online shopper.  

**Goals**  
- Track her order in real time.  
- Receive her order within the promised timeframe.  
- Get automatic notifications about changes or delays.  

**Frustrations**  
- Lack of transparency in tracking.  
- Having to contact the store whenever there’s a problem.  

**How she uses SLMS**  
- Enters her tracking number in a public portal.  
- Checks the location and estimated delivery date.  
- Receives automatic notifications (shipment, out for delivery, exceptions).  

# Scenarios

## João Ferreira’s (Logistics Manager) Scenarios

### Scenario 1: Daily monitoring of KPIs
- **Context:** João starts his day by reviewing the performance of the previous day’s transportation operations.  
- **Action:** He accesses the SLMS dashboard to check KPIs such as on-time delivery rates, cost per shipment, and carrier performance.  
- **Expected result:** He quickly identifies failures or areas for improvement, enabling immediate adjustments to the logistics strategy.  

### Scenario 2: Carrier comparison for new shipments
- **Context:** The company needs to ship a large volume of orders to another city. João must select the most efficient and cost-effective carrier.  
- **Action:** He accesses the SLMS carrier integration module, compares prices and transit times, and selects the best option.  
- **Expected result:** João makes an informed decision, reducing costs and improving delivery times.  

### Scenario 3: Performance reporting for management
- **Context:** João needs to present a logistics performance report to the company’s management.  
- **Action:** He exports detailed SLMS reports, including costs, delivery rates, and carrier performance.  
- **Expected result:** Management receives clear and reliable data, enabling strategic decisions about operational improvements.  

---

## Ana Martins’s (Warehouse Staff) Scenarios

### Scenario 4: Processing a new order
- **Context:** A new order arrives at the warehouse for shipping.  
- **Action:** Ana registers the order in SLMS, entering all required details (destination, weight, dimensions, assigned carrier).  
- **Expected result:** The order is correctly registered in the system and ready for shipment preparation.  

### Scenario 5: Generating shipping labels and documents
- **Context:** Ana needs to prepare several orders for dispatch at the end of the day.  
- **Action:** She uses SLMS to automatically generate shipping labels, transport guides, and manifests for each carrier.  
- **Expected result:** Each order leaves the warehouse with the correct documentation, reducing errors in the shipping process.  

### Scenario 6: Confirming carrier pickup
- **Context:** The carrier arrives at the warehouse to pick up orders.  
- **Action:** After the pickup, Ana updates SLMS to mark the orders as collected.  
- **Expected result:** The system records the pickup time, keeping information updated for both internal teams and customers.  

---

## Rita Lopes’s (Customer Service Representative) Scenarios

### Scenario 7: Quick order status check
- **Context:** A customer contacts support to ask about their order status.  
- **Action:** Rita searches for the order number in SLMS and accesses the real-time status.  
- **Expected result:** She immediately informs the customer of the order’s location and estimated delivery date.  

### Scenario 8: Resolving a delivery issue
- **Context:** A customer calls to report that they did not receive their order on time after being notified by SLMS about a delivery problem.  
- **Action:** Rita checks the exception reported by the driver (e.g., recipient absent, incorrect address, carrier delay), explains the details to the customer over the phone, makes the necessary updates to the order, records the resolution in SLMS, and closes the issue.  
- **Expected result:** The customer receives a clear explanation and immediate solution. SLMS is updated with the resolution, avoiding manual steps from the customer’s side.  

### Scenario 9: Handling a customer-reported issue
- **Context:** A customer reports a delivery issue through the SLMS public portal.  
- **Action:** Rita receives a notification in SLMS with the issue details (e.g., incorrect address, damaged package). She reviews the exception in the system, contacts the customer to clarify the situation, and records the resolution in SLMS, closing the exception.  
- **Expected result:** The issue reported by the customer is promptly addressed and registered in SLMS, ensuring transparent follow-up and efficient resolution.  

---

## Carlos Silva’s (Driver/Carrier) Scenarios

### Scenario 10: Checking delivery manifest
- **Context:** Carlos starts his shift and needs to know which packages he has to deliver.  
- **Action:** He opens the SLMS mobile app and checks the manifest with his daily deliveries.  
- **Expected result:** Carlos has a clear and organized view of packages, destinations, and priorities.  

### Scenario 11: Receiving optimized delivery routes
- **Context:** Carlos must deliver multiple packages along an urban route with many stops.  
- **Action:** He uses SLMS to receive an optimized route with the best delivery sequence.  
- **Expected result:** Reduced travel time and fuel consumption, completing deliveries more quickly.  

### Scenario 12: Recording proof of delivery (POD)
- **Context:** Carlos delivers a package to a customer.  
- **Action:** In SLMS, he records the delivery with either a customer’s digital signature or a photo of the package at the location.  
- **Expected result:** The delivery is validated in real time, visible to both the customer and the company.  

### Scenario 13: Reporting a delivery exception
- **Context:** Carlos attempts to deliver a package but the recipient is absent.  
- **Action:** He updates SLMS with the reason for the failed delivery (e.g., recipient not at home) and marks the package as an exception.  
- **Expected result:** The information is immediately available to both customer support and the customer.  

---

## Mariana Costa’s (Customer) Scenarios

### Scenario 14: Real-time order tracking
- **Context:** Mariana made an online purchase and wants to know where her package is.  
- **Action:** She accesses the SLMS public portal, enters her tracking number, and views the updated delivery status.  
- **Expected result:** Mariana sees the current location and estimated delivery date without needing to contact the store.  

### Scenario 15: Receiving automatic notifications
- **Context:** Mariana’s order moves through different shipping stages (shipped, out for delivery, failed attempt).  
- **Action:** SLMS automatically sends her notifications (via email or SMS) whenever there is a relevant update.  
- **Expected result:** Mariana feels informed and confident, always knowing what is happening with her order.  

### Scenario 16: Reporting a delivery issue
- **Context:** Mariana notices a problem with her order (e.g., wrong address, impossible delivery time, damaged package).  
- **Action:** She accesses the SLMS public portal, enters her tracking number, selects “Report a problem,” and describes the issue. The report is sent to Customer Service.  
- **Expected result:** The problem is registered in SLMS as an open exception, visible to the support team, who can contact Mariana and resolve it more quickly.  

---

# User Stories

## João Ferreira (Logistics Manager)

### US1 – “Daily monitoring of KPIs” (8)
As a Logistics Manager I want to access a dashboard with transportation KPIs so that I can monitor performance.  

**Acceptance Criteria**  
- **View daily KPIs**  
  - *Given* that I am logged into the SLMS as a Logistics Manager  
  - *When* I open the dashboard  
  - *Then* the system must show me KPIs such as on-time delivery rate, shipping costs and carrier performance  

### US2 – “Carrier comparison for new shipments” (13)
As a Logistics Manager I want to compare available carriers so that I can select the best option for new shipments.  

**Acceptance Criteria**  
- **Access carrier comparison**  
  - *Given* that I am logged into the SLMS as a Logistics Manager  
  - *When* I open the carrier integration module  
  - *Then* the system must display shipping costs and transit times for each carrier  

- **Select the optimal carrier**  
  - *Given* that I am reviewing the carrier list  
  - *When* I choose one carrier  
  - *Then* the system must assign that carrier to the shipment  

### US3 – “Generating performance reports” (8)
As a Logistics Manager I want to export reports so that I can share logistics performance with management.  

**Acceptance Criteria**  
- **Generate performance reports**  
  - *Given* that I am logged into the SLMS as a Logistics Manager  
  - *When* I select the reporting option  
  - *Then* the system must generate detailed reports with costs, on-time delivery rate and carrier performance  

- **Export reports**  
  - *Given* that the report is generated  
  - *When* I click on export  
  - *Then* the system must allow me to download it in formats such as PDF or Excel  

---

## Ana Martins (Warehouse Staff)

### US4 – “Processing a new order” (5)
As Warehouse Staff I want to register new orders in the system so that I can prepare them for shipment.  

**Acceptance Criteria**  
- **Register order details**  
  - *Given* that a new order arrives at the warehouse  
  - *When* I enter order information (destination, weight, dimensions, carrier) into the SLMS  
  - *Then* the system must save the order as ready for shipment  

### US5 – “Generating shipping labels and documents” (3)
As Warehouse Staff I want to generate labels and shipping documents so that I ensure each order is ready for dispatch.  

**Acceptance Criteria**  
- **Generate labels and documents**  
  - *Given* that I am logged into the SLMS as Warehouse Staff  
  - *When* I select an order  
  - *Then* the system must generate the shipping label, packing slip and shipping manifest for the carrier  

### US6 – “Confirming carrier pickup” (3)
As Warehouse Staff I want to confirm carrier pickups in order to keep the system updated.  

**Acceptance Criteria**  
- **Confirm pickup**  
  - *Given* that the carrier has collected the packages  
  - *When* I update the SLMS  
  - *Then* the system must mark the orders as “collected”  

---

## Rita Lopes (Customer Service Representative)

### US7 – “Quick order status lookup” (2)
As a Customer Service Representative I want to check the status of an order so that I can provide fast answers to customers.  

**Acceptance Criteria**  
- **Search for order status**  
  - *Given* that a customer contacts support  
  - *When* I search the order number in the SLMS  
  - *Then* the system must display the latest real-time status  

### US8 – “Resolving a delivery issue” (8)
As a Customer Service Representative I want to view delivery exceptions so that I can provide clear explanations and resolutions to customers.  

**Acceptance Criteria**  
- **Identify delivery issues**  
  - *Given* that I am logged into the SLMS as Customer Service  
  - *When* I search for an order with a reported exception  
  - *Then* the system must display the exception details (e.g. recipient absent, incorrect address, carrier delay)  

- **Provide resolution to customer**  
  - *Given* that I am handling a customer call about an exception  
  - *When* I explain the details and propose a solution  
  - *Then* the customer must receive clear information about the next steps  

- **Close exception**  
  - *Given* that the customer has been informed and the issue addressed  
  - *When* I update the SLMS with resolution notes  
  - *Then* the system must mark the exception as closed and keep the resolution history available for future reference  

### US9 – “Handling customer-reported issues” (13)
As a Customer Service Representative I want to receive notifications of problems reported by customers so that I can follow up and resolve them quickly.  

**Acceptance Criteria**  
- **Receive notification**  
  - *Given* that a customer has reported a problem  
  - *When* the SLMS registers it  
  - *Then* I must receive a notification with the order details and the issue description  

- **Follow up with customer**  
  - *Given* that I received a notification of a customer-reported problem  
  - *When* I contact the customer or update the order  
  - *Then* the SLMS must allow me to log resolution notes and close the exception  

---

## Carlos Silva (Driver / Carrier)

### US10 – “Viewing delivery manifest” (8)
As a Driver I want to view my delivery manifest so that I know all packages I need to deliver.  

**Acceptance Criteria**  
- **Access delivery manifest**  
  - *Given* that I am logged into the SLMS mobile app  
  - *When* I open my daily schedule  
  - *Then* the system must show a list of packages with addresses and priorities  

### US11 – “Receiving optimized delivery routes” (9)
As a Driver I want to receive optimized delivery routes so that I can complete deliveries more efficiently.  

**Acceptance Criteria**  
- **Receive route planning**  
  - *Given* that I am logged into the SLMS mobile app  
  - *When* I request my route  
  - *Then* the system must provide me with the optimal delivery sequence  

### US12 – “Recording proof of delivery (POD)” (8)
As a Driver I want to record proof of delivery so that I am able to validate that the package was delivered.  

**Acceptance Criteria**  
- **Register POD**  
  - *Given* that I have delivered a package  
  - *When* I collect a signature or take a delivery photo  
  - *Then* the SLMS must update the order as “delivered” in real time  

### US13 – “Reporting delivery exceptions” (5)
As a Driver I want to report delivery exceptions so that I can keep the system updated when delivery fails.  

**Acceptance Criteria**  
- **Report failed delivery**  
  - *Given* that I cannot deliver a package  
  - *When* I select a reason (e.g. recipient absent) in the mobile app  
  - *Then* the system must update the order status as “exception”  

---

## Mariana Costa (Customer)

### US14 – “Real-time order tracking” (21)
As a Customer I want to track my order in real-time so that I know its status and expected delivery date.  

**Acceptance Criteria**  
- **Track order**  
  - *Given* that I have a tracking number  
  - *When* I enter it in the SLMS customer portal  
  - *Then* the system must display the current location and estimated delivery date  

### US15 – “Receiving automatic notifications” (13)
As a Customer I want to receive notifications about my order so that I can stay informed.  

**Acceptance Criteria**  
- **Receive shipment updates**  
  - *Given* that my order status changes (e.g. shipped, out for delivery, failed attempt)  
  - *When* the event occurs  
  - *Then* the SLMS must send me an automatic notification (email or SMS)  

### US16 – “Reporting a delivery issue” (8)
As a Customer I want to report delivery issues through the tracking portal so that I can notify support and get faster resolutions.  

**Acceptance Criteria**  
- **Access report option**  
  - *Given* that I am in the SLMS customer portal with my tracking number  
  - *When* I open my order details  
  - *Then* the system must display an option to “Report a problem”  

- **Submit problem details**  
  - *Given* that I have identified a problem with my order  
  - *When* I select the “Report a problem” option and enter details (e.g. wrong address, damaged package, unavailable at delivery time)  
  - *Then* the SLMS must register the issue as an open exception linked to my order  

- **Notify customer service**  
  - *Given* that I have submitted a problem report  
  - *When* the exception is created  
  - *Then* the SLMS must notify a Customer Service Representative so that they can follow up with me  

---
