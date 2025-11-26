package Operimus.SLMS.business_layer.chatbot_module.service;

import Operimus.SLMS.business_layer.chatbot_module.dto.UpsertDocumentRequest;
import Operimus.SLMS.business_layer.chatbot_module.repository.AssistantDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Service for seeding initial knowledge base documents.
 * This helps bootstrap the RAG system with domain knowledge.
 */
@Service
public class DocumentSeederService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSeederService.class);

    private final DocumentManagementService documentManagementService;
    private final AssistantDocumentRepository documentRepository;

    public DocumentSeederService(DocumentManagementService documentManagementService,
                                 AssistantDocumentRepository documentRepository) {
        this.documentManagementService = documentManagementService;
        this.documentRepository = documentRepository;
    }

    /**
     * Seed initial knowledge base documents.
     * Call this once during setup to populate the system with base knowledge.
     * If documents already exist, this method will skip seeding to avoid duplicates.
     */
    public void seedInitialDocuments() {
        // Check if documents already exist
        long existingCount = documentRepository.count();
        if (existingCount > 0) {
            log.info("Knowledge base already contains {} documents. Skipping seeding.", existingCount);
            return;
        }

        log.info("Starting to seed initial knowledge base documents...");

        List<UpsertDocumentRequest> documents = getInitialDocuments();

        int count = 0;
        for (UpsertDocumentRequest doc : documents) {
            try {
                documentManagementService.upsertDocument(doc);
                count++;
                log.info("Seeded document: {}", doc.title());
            } catch (Exception e) {
                log.error("Failed to seed document: {}. Error: {}", doc.title(), e.getMessage());
            }
        }

        log.info("Finished seeding {} documents out of {}", count, documents.size());
    }

    /**
     * Get the list of initial documents to seed.
     * Comprehensive knowledge base covering all SLMS features and roles.
     */
    private List<UpsertDocumentRequest> getInitialDocuments() {
        return List.of(
                // ================================
                // SYSTEM OVERVIEW (3 documents)
                // ================================
                createSystemOverviewDoc(),
                createUserRolesDoc(),
                createSystemCapabilitiesDoc(),

                // ================================
                // GETTING STARTED (5 documents)
                // ================================
                createGettingStartedCustomerDoc(),
                createGettingStartedDriverDoc(),
                createGettingStartedWarehouseDoc(),
                createGettingStartedCustomerServiceDoc(),
                createGettingStartedLogisticsDoc(),

                // ================================
                // ORDER TRACKING (6 documents)
                // ================================
                createOrderTrackingDoc(),
                createOrderStatusesDoc(),
                createOrderHistoryDoc(),
                createOrderCreationDoc(),
                createOrderLifecycleDoc(),
                createShippingDocumentsDoc(),

                // ================================
                // DELIVERY EXCEPTIONS (8 documents)
                // ================================
                createReportDeliveryIssueDoc(),
                createExceptionTypesDoc(),
                createDriverConfirmationDoc(),
                createManagingExceptionsDoc(),
                createExceptionStatusWorkflowDoc(),
                createAssigningExceptionsDoc(),
                createContactingPartiesDoc(),
                createReschedulingReturnsDoc(),

                // ================================
                // WAREHOUSE OPERATIONS (3 documents)
                // ================================
                createWarehouseOrderManagementDoc(),
                createDocumentManagementDoc(),
                createWarehouseStatusUpdatesDoc(),

                // ================================
                // ADMINISTRATION (5 documents)
                // ================================
                createUserManagementDoc(),
                createWarehouseAdminDoc(),
                createDashboardStatsDoc(),
                createSystemOrderManagementDoc(),
                createRolePermissionsDoc(),

                // ================================
                // CUSTOMER SERVICE DASHBOARD (3 documents)
                // ================================
                createCustomerServiceDashboardDoc(),
                createPrioritySeverityDoc(),
                createExceptionFilteringDoc(),

                // ================================
                // NOTIFICATIONS & SUPPORT (2 documents)
                // ================================
                createNotificationPreferencesDoc(),
                createContactSupportDoc(),

                // ================================
                // TROUBLESHOOTING & FAQ (4 documents)
                // ================================
                createCustomerFAQDoc(),
                createDriverFAQDoc(),
                createWarehouseFAQDoc(),
                createCustomerServiceFAQDoc()
        );
    }

    // ================================
    // SYSTEM OVERVIEW DOCUMENTS
    // ================================

    private UpsertDocumentRequest createSystemOverviewDoc() {
        return new UpsertDocumentRequest(
                "What is SLMS - Smart Logistics Management System",
                Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal", "driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                The Smart Logistics Management System (SLMS) is a comprehensive end-to-end logistics platform designed to streamline the entire shipping and delivery process. SLMS connects customers, drivers, warehouse staff, customer service representatives, and logistics managers in one unified system.

                SLMS provides real-time order tracking, automated notifications, delivery exception management, route optimization, and comprehensive reporting capabilities. The system is built on modern cloud architecture and integrates with major shipping carriers to provide seamless logistics operations.

                Key Features:

                Order Management - Create, track, and manage orders from creation through delivery. Orders flow through multiple statuses: CREATED, ASSIGNED_TO_SHIPMENT, IN_TRANSIT, and DELIVERED. Customers can track their orders in real-time using tracking numbers, while warehouse staff and administrators have full visibility into all system orders.

                Real-Time Tracking - Every order in SLMS has a unique tracking number that customers can use to monitor their shipment's progress. The system records tracking events at each stage of the journey, including pickup, transit checkpoints, and final delivery. Customers receive automatic notifications via email or SMS as their order progresses.

                Delivery Exception Management - When delivery issues occur, SLMS provides a comprehensive exception management system. Customers can report problems like damaged packages, incorrect addresses, or missing items. Customer service representatives can then assign, track, and resolve these exceptions with full communication logging.

                Multi-Role Support - SLMS is designed to serve different user types with tailored interfaces. Customers access a portal to track orders and report issues. Drivers use a mobile PWA to confirm deliveries via QR code scanning. Warehouse staff manage order processing and documentation. Customer service representatives handle exception resolution. Logistics managers oversee the entire operation with analytics and reporting.

                Integration Capabilities - The system integrates with carrier APIs like ShipEngine and Shippo to obtain shipping rates and create shipping labels. It supports multiple warehouses, allowing orders to be routed to the optimal fulfillment center based on destination and inventory.

                Document Management - SLMS manages all shipping-related documents including shipping labels, commercial invoices, and proof of delivery records. These documents are stored securely and can be downloaded by authorized users at any time.

                Who Uses SLMS:

                Customers use SLMS to place orders, track shipments, and resolve delivery issues. They access the customer portal to view order history, download shipping documents, and communicate with customer service.

                Drivers use the driver PWA to receive delivery assignments and confirm deliveries by scanning QR codes on packages. This provides real-time delivery confirmation and updates order status immediately.

                Warehouse Staff use the admin console to process orders, update order statuses, upload shipping documents, and manage inventory across multiple warehouses.

                Customer Service Representatives use specialized tools to manage delivery exceptions, communicate with customers and carriers, reschedule deliveries, and initiate returns when needed.

                Logistics Managers and Administrators have full system access to manage users, configure warehouses, view comprehensive analytics, and oversee all logistics operations.

                SLMS is designed to scale from small operations to enterprise-level logistics, providing the tools and visibility needed to deliver exceptional customer experience while maintaining operational efficiency.
                """,
                null
        );
    }

    private UpsertDocumentRequest createUserRolesDoc() {
        return new UpsertDocumentRequest(
                "User Roles and Permissions in SLMS",
                Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal", "driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                SLMS implements a role-based access control system where each user type has specific permissions and capabilities designed for their responsibilities. Understanding these roles helps you know what you can do in the system and who to contact for different needs.

                Anonymous Users (Public Access):
                Anonymous users are visitors who haven't logged in. They can access the public landing page to track orders using tracking numbers without needing an account. This allows customers to check delivery status even if they didn't create an account. Anonymous users cannot report issues, view order history, or access personal information.

                Customer Role:
                Customers are registered users who place and receive orders. When you log into the customer portal, you can view all your orders, both current and historical. You can track order progress in real-time, download shipping documents like labels and delivery confirmations, and report delivery issues through the exception management system. Customers can also manage notification preferences to choose how they want to receive updates about their shipments. The customer role is designed for order recipients who need visibility into their deliveries.

                Driver Role:
                Drivers are delivery personnel who use the mobile driver PWA to complete deliveries. This role has access to delivery confirmation features, primarily the QR code scanning functionality. When drivers arrive at a delivery location, they scan the QR code on the package to confirm delivery. This immediately updates the order status to DELIVERED and triggers customer notifications. Drivers can only see and interact with orders assigned to their shipments. This role is streamlined for mobile use during active deliveries.

                Warehouse Staff Role:
                Warehouse staff are responsible for order processing and fulfillment. They use the admin console to create orders, update order statuses as packages move through warehouse processes, upload shipping documents and labels, and manage inventory. Warehouse staff can view all orders in the system and filter them by status to prioritize their work. They can download and upload documents related to orders and shipments. This role is essential for the operational backbone of order fulfillment.

                Customer Service Representative Role:
                Customer service representatives are specialized support staff who handle delivery issues and customer inquiries. This role has comprehensive access to the delivery exception management system. Representatives can view all delivery exceptions across the system, filter them by status, priority, and severity to prioritize work, assign exceptions to team members for resolution, and communicate with both customers and carriers about issues. They can also reschedule deliveries, initiate return requests, and close resolved exceptions with resolution notes. Customer service reps can access full order history and tracking information to assist customers effectively. They also view dashboard KPIs to monitor support performance.

                Administrator and Logistics Manager Roles:
                Administrators and logistics managers have the highest level of system access. They can manage user accounts including creating, updating, and deleting users across all roles. They configure and manage warehouse locations and settings. They access comprehensive dashboard statistics and reports covering order volumes, delivery success rates, exception trends, and operational metrics. They have full visibility into all orders, exceptions, and system activities. Administrators manage system-wide settings and permissions.

                Role Hierarchy:
                The system implements a clear permission hierarchy. Administrators can perform all actions. Customer service representatives can manage exceptions and view orders but cannot modify system configuration. Warehouse staff can manage orders and documents but cannot access exception management or user administration. Drivers have the most limited access, focused only on delivery confirmation. Customers can only view and track their own orders.

                Requesting Access:
                If you need access to features beyond your current role, contact your system administrator or logistics manager. They can assign additional roles or upgrade your permissions based on your responsibilities. User roles can be changed at any time by administrators through the user management interface.
                """,
                null
        );
    }

    private UpsertDocumentRequest createSystemCapabilitiesDoc() {
        return new UpsertDocumentRequest(
                "System Capabilities Overview - What SLMS Can Do",
                Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal", "driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                SLMS provides a comprehensive suite of logistics capabilities designed to handle every aspect of order fulfillment and delivery management. This document outlines what the system can do across all functional areas.

                Order Creation and Management:
                The system allows warehouse staff and administrators to create new orders with complete shipment details including origin and destination addresses, package dimensions and weight, and service level requirements. Orders can be created individually or imported in bulk. Once created, orders flow through a defined lifecycle with automatic status updates at each stage. The system validates addresses, calculates shipping costs using integrated carrier APIs, and generates shipping labels automatically.

                Real-Time Order Tracking:
                SLMS provides comprehensive tracking capabilities for all orders. Every order receives a unique tracking number that customers can use to monitor progress. The system records tracking events at each milestone including order creation, warehouse processing, carrier pickup, transit updates, out for delivery, and final delivery confirmation. These events appear in a chronological timeline that customers can view through the tracking page. The system also provides estimated delivery dates based on shipping method and destination.

                Warehouse Operations:
                Warehouse staff can process orders through multiple fulfillment stages. The system supports order picking, packing, and shipping workflows. Staff can update order statuses to reflect current processing state, upload required shipping documents including commercial invoices and packing lists, generate and print shipping labels directly from the system, and manage orders across multiple warehouse locations. The system helps optimize warehouse operations by providing order lists filtered by status and priority.

                Driver Delivery Confirmation:
                The driver PWA enables mobile delivery confirmation through QR code scanning. Each package has a unique QR code that drivers scan upon delivery. This instantly updates the order status to DELIVERED, records the delivery timestamp, triggers automatic customer notifications, and updates warehouse and admin dashboards. The QR code system ensures accurate delivery confirmation and prevents errors associated with manual entry.

                Delivery Exception Management:
                When delivery problems occur, SLMS provides robust exception handling. Customers can report issues like damaged packages, incorrect items, non-receipt, or delivery to wrong address. These reports create delivery exception records that enter the exception management workflow. Customer service representatives can view all exceptions, assign them to team members, set priority and severity levels, track exception status from open through resolution, communicate with customers and carriers, reschedule deliveries or initiate returns, and close exceptions with resolution notes. The system maintains a complete history of all exception handling activities.

                Communication and Notifications:
                SLMS sends automatic notifications to keep all parties informed. Customers receive email or SMS updates when orders are created, shipped, out for delivery, and delivered. They also receive notifications about delivery exceptions and their resolutions. Customer service representatives can send manual notifications to customers about their specific cases. The notification system is configurable, allowing customers to choose their preferred communication channels.

                Document Management:
                The system manages all shipping-related documents. It generates shipping labels with carrier-compliant formats, stores commercial invoices for international shipments, maintains proof of delivery records, allows document upload by warehouse staff, and enables document download by authorized users. All documents are linked to their orders and searchable.

                Analytics and Reporting:
                Logistics managers and administrators access comprehensive analytics including total order volume and trends, delivery success rates, average delivery times, exception rates by type and severity, warehouse performance metrics, driver performance statistics, and customer service KPIs. These reports help identify operational improvements and measure performance against targets.

                User and Access Management:
                Administrators can create and manage user accounts for all roles, assign role-based permissions, update user information and contact details, deactivate or delete users, and audit user activity through system logs. This ensures secure access control and user accountability.

                Integration Capabilities:
                SLMS integrates with external systems including carrier APIs for rate shopping and label generation, notification services for email and SMS, authentication systems through Keycloak for secure single sign-on, and document storage using MinIO for scalable file management. These integrations enable SLMS to function as part of a broader enterprise technology ecosystem.
                """,
                null
        );
    }

    // ================================
    // GETTING STARTED DOCUMENTS
    // ================================

    private UpsertDocumentRequest createGettingStartedCustomerDoc() {
        return new UpsertDocumentRequest(
                "Getting Started Guide for Customers",
                Set.of("CUSTOMER", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal"),
                Set.of(),
                "en-US",
                """
                Welcome to the Smart Logistics Management System (SLMS) Customer Portal! This guide will help you get started with tracking your orders, managing your shipments, and resolving any delivery issues. Whether you're a new customer or just need a refresher, this guide covers everything you need to know.

                Creating Your Account:
                If you don't already have an account, you can create one through the customer portal registration page. You'll need to provide your email address, create a password, and fill in basic contact information including your name, phone number, and default delivery address. Once registered, you'll receive a confirmation email with a link to verify your account. Click the verification link to activate your account and log in for the first time.

                If you prefer, you can also track orders without creating an account by using the tracking number on the public landing page. However, creating an account gives you access to order history, document downloads, and the ability to report delivery issues.

                Logging In:
                To access your customer portal, navigate to the login page and enter your registered email and password. If you forget your password, use the "Forgot Password" link to reset it via email. Once logged in, you'll see your dashboard with an overview of your current and recent orders.

                Tracking Your Orders:
                Your customer portal dashboard displays all orders associated with your account. Each order shows its current status, tracking number, destination address, and estimated delivery date. To view detailed tracking information for an order, click on the order card or tracking number. This opens the order detail page where you can see the complete tracking timeline with all events from order creation through delivery.

                The tracking timeline shows key milestones including when the order was created, when it was processed at the warehouse, when it was picked up by the carrier, transit checkpoints, when it's out for delivery, and final delivery confirmation. Each event includes a timestamp so you know exactly when it occurred.

                You can also track orders using just the tracking number without logging in. Visit the public tracking page on the landing page and enter your tracking number. This is useful if you received a tracking number via email or SMS and want to quickly check status without logging into your account.

                Understanding Order Statuses:
                Orders in SLMS progress through several statuses. CREATED means your order has been entered into the system and is awaiting warehouse processing. READY_FOR_PICKUP indicates the warehouse has prepared your package and it's ready for carrier pickup. COLLECTED means the carrier has picked up the package from the warehouse. ASSIGNED_TO_SHIPMENT shows the package has been assigned to a specific shipment for delivery. IN_TRANSIT means your package is on its way to you, moving through the carrier's network. DELIVERED indicates successful delivery confirmation. CANCELLED means the order was cancelled before completion.

                Most orders follow this sequence: CREATED → READY_FOR_PICKUP → COLLECTED → ASSIGNED_TO_SHIPMENT → IN_TRANSIT → DELIVERED. You'll receive automatic notifications as your order progresses through these stages.

                Downloading Shipping Documents:
                For each order, you can download important shipping documents. The order detail page has a documents section where you can access shipping labels, commercial invoices for international shipments, and proof of delivery records once your package is delivered. Click the download button next to any document to save it to your device. These documents are useful for customs, recordkeeping, or if you need to file a claim.

                Reporting Delivery Issues:
                If something goes wrong with your delivery, you can report it directly through the customer portal. Common issues include damaged packages, incorrect items received, packages not received when marked as delivered, or delivery to the wrong address. To report an issue, go to the order detail page and click "Report Issue" or "Create Exception." Select the type of problem from the dropdown menu, provide a detailed description of what happened, and optionally attach photos if relevant (for example, pictures of damaged packaging).

                Once submitted, your issue creates a delivery exception that enters our customer service workflow. A customer service representative will be assigned to investigate and resolve your issue. You can view the status of your exception in the customer portal and see updates as the representative works on your case.

                Managing Notification Preferences:
                SLMS can send you updates about your orders via email and SMS. By default, you'll receive email notifications for major order events. To customize your notification preferences, go to your account settings. You can choose which events trigger notifications, select email or SMS delivery, and set your preferred language. We recommend keeping notifications enabled for delivery confirmation and exception updates so you never miss important information about your shipments.

                Viewing Order History:
                Your customer portal maintains a complete history of all your orders. You can filter orders by status to see only active deliveries or review past deliveries. The search function lets you find specific orders by tracking number, destination, or date range. This history is useful for tracking package delivery patterns, reordering items, or pulling up old tracking information.

                Getting Help:
                If you have questions or need assistance, you can contact customer support through the help section of the customer portal. Our chatbot assistant is available 24/7 to answer common questions about tracking, order status, and system features. For more complex issues, you can reach out to a human customer service representative who can provide personalized assistance with your specific situation.

                Next Steps:
                Now that you understand the basics, explore your customer portal to see your orders, set up your notification preferences, and familiarize yourself with the tracking interface. If you have any active orders, try viewing their tracking timelines to see where they are in the delivery process. Remember, if any issues arise, the exception reporting feature is there to help you get them resolved quickly.
                """,
                null
        );
    }

    private UpsertDocumentRequest createGettingStartedDriverDoc() {
        return new UpsertDocumentRequest(
                "Getting Started Guide for Drivers",
                Set.of("DRIVER", "ADMIN"),
                Set.of("driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                Welcome to the SLMS Driver Progressive Web App (PWA)! As a delivery driver, this mobile application is your essential tool for confirming deliveries and updating order statuses in real-time. This guide will walk you through everything you need to know to use the driver app effectively.

                About the Driver PWA:
                The SLMS driver app is a Progressive Web App, which means it works on any smartphone or tablet through a web browser but provides a native app-like experience. It works on both iOS and Android devices without requiring app store downloads or updates. The PWA is optimized for mobile use with large touch targets, offline capabilities, and quick-loading interfaces designed for use in the field.

                Accessing the Driver App:
                Your logistics manager will provide you with login credentials for the driver portal. Open your mobile browser (Safari on iOS, Chrome on Android) and navigate to the driver PWA URL. Bookmark this page or add it to your home screen for quick access. The PWA can be installed to your home screen just like a regular app - look for the "Add to Home Screen" option in your browser menu.

                Logging In:
                Enter your assigned username and password on the login screen. Make sure to keep your credentials secure as they're tied to your delivery confirmations. If you have trouble logging in or forget your password, contact your warehouse manager or logistics administrator who can reset it for you.

                Understanding Your Delivery Assignments:
                When you log in, you'll see a list of deliveries assigned to your route for the day. Each delivery shows the destination address, package details, and delivery priority. The app may integrate with mapping services to help you plan your optimal route, though your logistics team may also provide route planning separately.

                Your primary responsibility is confirming deliveries using the QR code scanning feature, which we'll cover in detail next.

                Confirming Deliveries with QR Code Scanning:
                Every package in the SLMS system has a unique QR code attached to it. When you successfully deliver a package to a customer, you confirm the delivery by scanning this QR code. Here's how the process works:

                When you arrive at the delivery location and hand the package to the customer or leave it in a safe place according to their instructions, open the driver app on your phone. Look for the "Scan QR Code" or "Confirm Delivery" button. This will activate your phone's camera. Point the camera at the QR code on the package label. The app will automatically detect and scan the code.

                Once scanned, the app immediately sends a confirmation to the SLMS backend. This updates the order status to DELIVERED, records the exact timestamp of delivery, triggers automatic notifications to the customer informing them of successful delivery, and updates dashboards for warehouse staff and administrators.

                The QR code system is designed to be extremely reliable. The unique codes ensure you're confirming the correct package, the instant upload means data is never lost, and the timestamp provides proof of delivery time. This system eliminates manual data entry errors and ensures customers receive immediate delivery confirmation.

                Handling QR Code Issues:
                If you have trouble scanning a QR code due to poor lighting, damaged labels, or camera issues, try these troubleshooting steps. Ensure your camera lens is clean and free of smudges. Move to better lighting if the code is hard to read in shadows. Hold your phone steady and allow the camera to focus properly. If the code is damaged or unreadable, contact your dispatcher for guidance. They may provide an alternative confirmation method for that specific delivery.

                The app requires internet connectivity to upload delivery confirmations. If you're in an area with poor cellular signal, the app will queue the confirmation and upload it automatically once you regain connectivity. This ensures no deliveries are lost even in areas with spotty coverage.

                Viewing Your Delivery Status:
                The driver app shows which deliveries you've completed and which remain on your route. Completed deliveries are marked with a checkmark and timestamp. Pending deliveries show in your active list. This helps you track your progress throughout the day and ensures you don't miss any stops.

                Reporting Delivery Problems:
                Sometimes deliveries can't be completed as planned. Common scenarios include no one home to receive the package, address not found, access denied to building or property, or customer refused delivery. While the driver app focuses primarily on confirmation, you should report unsuccessful delivery attempts to your dispatcher immediately. They can coordinate with customer service to reschedule or resolve the issue.

                Do not mark packages as delivered if they weren't actually delivered. The QR code system creates an audit trail, and false confirmations can cause serious customer service issues. Always be honest about delivery status.

                Best Practices for Drivers:
                Keep your phone charged throughout your shift as the app is essential for confirmations. Scan QR codes immediately after each successful delivery rather than batching them later. This ensures real-time updates and accurate delivery timestamps. Verify the delivery address matches the address on your route sheet before handing over the package. Keep packages secure in your vehicle until delivery. Treat customer packages with care to prevent damage. Follow all customer delivery instructions such as "leave at back door" or "ring doorbell."

                Privacy and Security:
                Your driver account is tied to your delivery confirmations, which creates accountability and tracking for the logistics chain. Never share your login credentials with others. Log out of the driver app at the end of your shift, especially if using a shared device. Report any suspected account security issues to your manager immediately.

                Getting Help:
                If you encounter technical issues with the driver app, such as login problems, camera not working, or confirmation upload failures, contact your warehouse manager or logistics dispatcher immediately. They can troubleshoot the issue or provide alternative delivery confirmation methods if needed. The SLMS system has support staff available to help with driver app issues.

                Next Steps:
                Now that you understand the driver app, practice scanning QR codes before your first route. Familiarize yourself with the interface and delivery list display. Make sure you can successfully scan and confirm test deliveries. Once you're comfortable with the process, you'll be ready to handle real deliveries efficiently. Remember, the QR code scanning system is designed to be quick and simple - with practice, confirmations take just a few seconds per delivery.
                """,
                null
        );
    }

    private UpsertDocumentRequest createGettingStartedWarehouseDoc() {
        return new UpsertDocumentRequest(
                "Getting Started Guide for Warehouse Staff",
                Set.of("WAREHOUSE_STAFF", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Welcome to the SLMS Admin Console for Warehouse Operations! As warehouse staff, you play a critical role in processing orders, managing inventory, and ensuring packages are prepared correctly for delivery. This guide will help you get started with the warehouse management features of SLMS.

                Accessing the Admin Console:
                Your warehouse manager or system administrator will provide you with login credentials for the SLMS admin console. Navigate to the admin console URL in your web browser and enter your username and password. The admin console is optimized for desktop use, so we recommend accessing it from a workstation computer rather than a mobile device for the best experience.

                Understanding Your Role:
                As warehouse staff, your primary responsibilities in SLMS include creating new orders in the system when customers place them, updating order statuses as packages move through warehouse processing stages, uploading shipping documents and labels to order records, managing inventory and warehouse locations, and coordinating with drivers and carriers for pickups.

                The admin console gives you access to all orders in the system, though you'll primarily work with orders in your assigned warehouse. You can filter and search orders to find the ones you need to process.

                Creating New Orders:
                When a new order comes in through your sales channels, you'll create it in SLMS to begin the fulfillment process. Navigate to the Orders section and click "Create New Order." You'll need to enter several pieces of information:

                Customer information including name, email, phone number, and delivery address. Package details such as dimensions, weight, and contents. Service level requirements like standard ground shipping or expedited delivery. Origin warehouse location. Any special handling instructions.

                Once you've entered all required information, click Submit to create the order. SLMS will assign it a unique tracking number and set its initial status to CREATED. The order now enters your warehouse workflow for processing.

                Managing Order Statuses:
                As orders move through your warehouse, you'll update their status to reflect their current processing stage. The typical warehouse workflow looks like this:

                CREATED - Order is newly entered and awaiting processing. This is the initial status when you create an order. READY_FOR_PICKUP - Your team has picked, packed, and prepared the package. Shipping label is attached and the package is staged for carrier pickup. COLLECTED - The carrier has physically picked up the package from your warehouse. This usually triggers the handoff to the carrier's systems.

                To update an order status, find the order in your orders list, open the order detail page, and select the new status from the status dropdown. Add any relevant notes about the status change, then click Update. The system will record the status change with a timestamp and your user ID for tracking.

                Status updates are important because they trigger customer notifications, update tracking information, and provide visibility to other team members about order progress.

                Uploading Shipping Documents:
                Every order requires certain documents for successful delivery. The most important is the shipping label, which carriers use to route the package and which contains the tracking barcode. For international shipments, you'll also need commercial invoices. Other documents might include packing lists, customs forms, or special handling instructions.

                To upload documents to an order, open the order detail page and navigate to the Documents section. Click "Upload Document" and select the document type from the dropdown. Choose the file from your computer - SLMS accepts common formats like PDF, PNG, and JPEG. Add a description if needed, then click Upload.

                The document is now attached to the order record and can be accessed by authorized users including the customer, other warehouse staff, and administrators. Customers can download shipping labels and invoices from their customer portal. This centralized document management ensures everyone has access to the information they need.

                Generating Shipping Labels:
                SLMS integrates with carrier APIs to generate shipping labels automatically. When you're ready to ship an order, you can use the label generation feature. Select the order, choose the carrier and service level, verify the destination address, and click Generate Label. SLMS will call the carrier API, create the label, and automatically upload it to the order documents. You can then print the label and attach it to the package.

                This integration saves time compared to manually creating labels in carrier systems and ensures the label information is automatically synced with the order record.

                Managing Multiple Warehouses:
                If your organization operates multiple warehouse locations, SLMS supports multi-warehouse management. Each order is associated with a specific warehouse location. You can filter orders by warehouse to see only those relevant to your facility. This helps prevent confusion and ensures orders are fulfilled from the correct inventory location.

                Warehouse administrators can configure warehouse settings including address information, operating hours, and fulfillment capacity. This helps the logistics team route orders to the optimal warehouse based on customer location and inventory availability.

                Searching and Filtering Orders:
                As your order volume grows, finding specific orders becomes important. The admin console provides powerful search and filtering tools. You can search by tracking number to quickly pull up a specific order, filter by status to see all orders in a particular stage, filter by date range to view orders from a specific period, or filter by warehouse to see only your facility's orders.

                Use these tools to prioritize your work, identify bottlenecks, and ensure no orders get overlooked in the fulfillment process.

                Coordinating with Drivers and Carriers:
                When packages are ready for pickup, you'll coordinate with drivers and carriers. Update the order status to READY_FOR_PICKUP to signal that the package is prepared and staged. Ensure the shipping label is clearly visible on the package. Verify the package is in the designated pickup area. When the driver or carrier arrives and takes the package, update the status to COLLECTED.

                This coordination ensures smooth handoffs and prevents packages from sitting unnecessarily in your warehouse.

                Best Practices for Warehouse Staff:
                Update order statuses promptly as packages move through processing. Don't let status updates lag behind reality, as this confuses customers and other team members. Double-check addresses before generating shipping labels. Incorrect addresses cause delivery failures and costly returns. Upload all required documents before updating status to READY_FOR_PICKUP. This ensures complete information is available when the carrier picks up the package. Use clear, descriptive notes when updating statuses or uploading documents. This helps other team members understand what happened with an order. Verify package dimensions and weight are accurate. Incorrect information can cause carrier surcharges or delivery issues.

                Getting Help:
                If you encounter issues with the admin console or have questions about warehouse procedures, contact your warehouse manager or system administrator. They can provide guidance on SLMS features and help troubleshoot any problems. The system also has a help section with documentation about warehouse operations. The SLMS chatbot assistant can answer common questions about features and workflows.

                Next Steps:
                Now that you understand the basics of warehouse operations in SLMS, spend some time exploring the admin console. Practice creating test orders, updating statuses, and uploading documents in a test environment if available. Familiarize yourself with the search and filtering tools. Once you're comfortable with the interface, you'll be ready to efficiently process real orders and keep your warehouse operations running smoothly.
                """,
                null
        );
    }

    private UpsertDocumentRequest createGettingStartedCustomerServiceDoc() {
        return new UpsertDocumentRequest(
                "Getting Started Guide for Customer Service Representatives",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Welcome to SLMS Customer Service! As a customer service representative, you're the critical link between customers and successful delivery outcomes. Your role focuses on managing delivery exceptions, resolving customer issues, and ensuring excellent customer experience. This guide will help you get started with the customer service features in the SLMS admin console.

                Accessing the Customer Service Dashboard:
                Log into the SLMS admin console using your customer service representative credentials. Once logged in, you'll see the customer service dashboard which provides an overview of delivery exceptions requiring attention. This dashboard is your command center for managing customer issues and tracking resolution progress.

                Understanding Your Role:
                As a customer service representative, your primary responsibilities include monitoring delivery exceptions reported by customers, investigating issues and coordinating with carriers and customers, assigning exceptions to team members for resolution, rescheduling deliveries or initiating returns when needed, communicating with customers about issue status and resolution, closing resolved exceptions with resolution notes, and maintaining high customer satisfaction through responsive support.

                Your work ensures that when delivery problems occur, they're handled quickly and effectively to minimize customer frustration and maintain trust in the logistics system.

                The Delivery Exception System:
                When customers encounter delivery problems, they report them through the customer portal. This creates a delivery exception record in SLMS. Common exception types include damaged packages, incorrect items received, package not received despite delivery confirmation, delivery to wrong address, late delivery beyond estimated date, and customer requests for delivery changes.

                Each exception enters a workflow from creation through resolution. Your job is to guide exceptions through this workflow efficiently.

                Viewing and Filtering Exceptions:
                The customer service dashboard shows all delivery exceptions in the system. You can filter exceptions by several criteria to prioritize your work:

                Status filters show exceptions by their current workflow stage - OPEN for newly reported issues, ASSIGNED for issues assigned to a representative, IN_PROGRESS for issues being actively worked, RESOLVED for issues with solutions implemented, and CLOSED for fully completed issues.

                Priority filters help you identify urgent issues - HIGH priority for time-sensitive or high-value shipments, MEDIUM priority for standard issues, and LOW priority for minor concerns.

                Severity filters indicate customer impact - CRITICAL for complete delivery failures, MAJOR for significant problems, and MINOR for small issues.

                Use these filters to ensure the most important issues get immediate attention while still tracking all exceptions through resolution.

                Exception Assignment and Workflow:
                When a new exception is reported, it starts in OPEN status. A supervisor or team lead can assign it to a specific customer service representative. Once assigned, the status changes to ASSIGNED. The assigned representative then begins investigating the issue, changing the status to IN_PROGRESS.

                To assign an exception to yourself or a team member, open the exception detail page, click "Assign Exception," and select the representative from the dropdown. Add assignment notes if needed. The assigned representative will see the exception in their assigned issues list and can begin working on it.

                Investigating and Resolving Exceptions:
                When you're assigned an exception, start by reviewing all available information. Read the customer's reported issue description carefully. Check the order's tracking history to understand what happened. Review any attached documents or photos from the customer. Look at the carrier tracking information to see where the package actually is.

                Based on your investigation, determine the appropriate resolution approach. For damaged packages, you might coordinate with the carrier for an insurance claim and arrange a replacement shipment. For non-receipt issues, check if the driver confirmed delivery via QR code and investigate potential misdelivery or theft. For incorrect items, arrange return pickup and correct item shipment. For delivery address errors, update the address and reschedule delivery if the package hasn't been delivered yet.

                Communicating with Customers:
                Throughout the exception resolution process, keep customers informed. SLMS provides communication tools to send updates to customers about their issues. When you begin working on an exception, send an initial acknowledgment to the customer confirming you've received their report and are investigating. Provide an estimated timeline for resolution.

                As you make progress, send update messages about what you've discovered and what actions you're taking. If you need additional information from the customer, send a request message asking specific questions. When the issue is resolved, send a resolution message explaining what was done and confirming the outcome.

                Clear, proactive communication prevents customer frustration and builds trust even when things have gone wrong with their delivery.

                Rescheduling Deliveries:
                If a delivery needs to be rescheduled due to address corrections, customer unavailability, or failed delivery attempts, you can initiate rescheduling through the exception management system. Contact the carrier to arrange a new delivery date. Update the order with the rescheduled information. Notify the customer of the new delivery date. Update the exception with notes about the rescheduling.

                This ensures customers know when to expect their package and prevents repeated delivery failures.

                Initiating Returns:
                When a return is necessary due to incorrect items, damaged goods, or customer rejection, you can initiate the return process. Create a return authorization in the system. Generate a return shipping label for the customer. Send the label to the customer with return instructions. Coordinate with the warehouse to expect the return and process it appropriately. Update the exception with return tracking information.

                Efficient return handling minimizes customer effort and speeds up replacement or refund processing.

                Closing Resolved Exceptions:
                Once you've successfully resolved an issue, close the exception to complete the workflow. Update the exception status to RESOLVED. Add detailed resolution notes explaining what was done and the outcome. If applicable, attach any supporting documents like replacement tracking numbers or return authorizations. Get customer confirmation that they're satisfied with the resolution. Change status to CLOSED to mark the issue as fully complete.

                Comprehensive resolution notes are important for tracking patterns, training new staff, and handling follow-up questions about the same issue.

                Using the Customer Service Dashboard KPIs:
                The customer service dashboard displays key performance indicators that help you and your team monitor performance. These might include total open exceptions, average time to resolution, exception resolution rate, customer satisfaction scores, and exceptions by type or severity.

                Review these KPIs regularly to identify trends, spot potential systemic issues that need addressing, and ensure your team is meeting performance targets. If you notice increasing exceptions of a particular type, report this to logistics managers so they can address the root cause.

                Accessing Customer and Order Information:
                As a customer service representative, you have access to full order history and customer information to assist with issue resolution. You can view all of a customer's past orders, check order tracking timelines, access shipping documents, and see previous exceptions and their resolutions.

                This comprehensive access ensures you have all the context needed to help customers effectively.

                Best Practices for Customer Service Representatives:
                Respond to new exceptions within a defined timeframe, such as within 2 hours for high-priority issues. Keep exception status updated as you work through resolution. Set accurate expectations with customers about resolution timelines. Document all actions and communications in exception notes. Escalate complex or unresolvable issues to supervisors promptly. Look for patterns in exceptions that might indicate larger systemic problems. Maintain a helpful, empathetic tone in all customer communications.

                Getting Help:
                If you encounter an exception you're unsure how to handle, consult with senior representatives or your supervisor. They can provide guidance on resolution approaches and help with complex cases. The admin console help section has documentation about exception management workflows. The SLMS chatbot can answer questions about system features and procedures.

                Next Steps:
                Now that you understand the customer service features in SLMS, explore the exception management interface. Review the dashboard KPIs and filtering options. Practice viewing exception details and understanding the workflow stages. If training exceptions are available, practice assigning, investigating, and closing them. Once you're comfortable with the system, you'll be ready to provide excellent customer service and efficiently resolve delivery issues.
                """,
                null
        );
    }

    private UpsertDocumentRequest createGettingStartedLogisticsDoc() {
        return new UpsertDocumentRequest(
                "Getting Started Guide for Logistics Managers and Administrators",
                Set.of("ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Welcome to SLMS Administration! As a logistics manager or system administrator, you have comprehensive access to all SLMS features and are responsible for overseeing the entire logistics operation. This guide will help you get started with the administrative and management capabilities of the system.

                Accessing the Admin Console:
                Log into the SLMS admin console using your administrator credentials. Your administrator account has elevated privileges that allow you to access all areas of the system including order management, user administration, warehouse configuration, analytics and reporting, system settings, and all customer service and warehouse features.

                Understanding Your Role:
                As an administrator or logistics manager, your responsibilities span the entire logistics operation. You oversee order flow and fulfillment processes, manage user accounts and permissions, configure warehouse locations and settings, monitor performance through analytics and dashboards, identify and resolve operational bottlenecks, ensure carrier integrations are working correctly, and set policies and procedures for logistics operations.

                Your broad access and responsibilities make you the guardian of smooth, efficient logistics operations across all teams and functions.

                Dashboard and Analytics:
                The admin console home screen displays comprehensive operational dashboards with key metrics about your logistics performance. These typically include total order volume and trends over time, delivery success rates and on-time performance, average time from order creation to delivery, exception rates by type and severity, warehouse performance metrics such as processing times, driver performance and delivery confirmation rates, customer service KPIs including average resolution time, and carrier performance comparisons.

                Use these dashboards daily to spot trends, identify problems early, and make data-driven decisions about resource allocation and process improvements. If you notice degrading performance in any area, investigate immediately to prevent small issues from becoming major problems.

                User Management:
                One of your key responsibilities is managing user accounts across all roles. You can create new user accounts for customers, drivers, warehouse staff, customer service representatives, and other administrators. When creating accounts, assign appropriate roles and permissions based on each user's responsibilities.

                To create a new user, navigate to the User Management section, click "Add New User," and enter the user's information including name, email, phone number, and assigned role. The system will generate login credentials that you can provide to the new user. You can also update existing user information, reset passwords for users who are locked out, deactivate users who no longer need access, and audit user activity through system logs.

                Proper user management ensures secure access control and accountability throughout your logistics operations.

                Warehouse Configuration and Management:
                If your organization operates multiple warehouse locations, you'll configure and manage them through the admin console. Warehouse configuration includes setting up the warehouse address and contact information, defining operating hours and capacity, assigning warehouse staff users to specific locations, and configuring fulfillment rules and inventory management.

                Proper warehouse configuration ensures orders are routed to the optimal fulfillment location based on destination, inventory availability, and warehouse capacity. You can view performance metrics for each warehouse to compare efficiency and identify which locations may need additional resources or process improvements.

                Order Management and Oversight:
                While warehouse staff create and process most orders, as an administrator you have visibility into all orders across all warehouses and customers. Use this comprehensive view to monitor order flow, identify bottlenecks where orders are stuck in particular statuses, respond to escalated customer service issues, and generate reports about order patterns and fulfillment performance.

                You can also intervene directly in orders when needed, such as manually updating status for orders with issues, reassigning orders to different warehouses, or cancelling problematic orders with appropriate documentation.

                Carrier Integration Management:
                SLMS integrates with carrier APIs like ShipEngine and Shippo to obtain shipping rates and generate labels. As an administrator, you oversee these integrations to ensure they're working correctly. Monitor API usage and any error rates, update carrier API credentials if needed, configure rate shopping rules to select the best carrier for each shipment, review carrier performance metrics, and troubleshoot integration issues when they arise.

                Effective carrier integration management ensures your logistics operations can scale efficiently without manual processes.

                Managing Delivery Exceptions:
                While customer service representatives handle day-to-day exception resolution, as an administrator you monitor overall exception trends and intervene when necessary. Review exception analytics to identify patterns - if you notice increasing damaged package reports from a particular carrier, for example, you might need to address packaging procedures or switch carriers. Escalated exceptions that customer service can't resolve come to you for final decision-making. You also set policies for exception handling, such as automatic refund thresholds or replacement procedures.

                Customer Service Team Oversight:
                If you manage a customer service team, use the customer service dashboards to monitor team performance. Track metrics like average time to first response on new exceptions, average time to full resolution, exception backlog and aging, representative workload distribution, and customer satisfaction scores if available.

                Use this data to balance workload across representatives, identify training needs, and ensure your team is meeting service level agreements.

                System Configuration and Settings:
                As an administrator, you can configure various system-wide settings. These might include default notification preferences for different user roles, automated workflow rules for order processing, integration settings for external systems, security policies and access controls, and document storage and retention policies.

                Changes to system configuration affect all users, so make changes carefully and communicate them to your team when they might impact daily operations.

                Reporting and Data Export:
                Beyond real-time dashboards, SLMS provides reporting capabilities for deeper analysis. You can generate reports on order volume by time period, warehouse, or customer, delivery performance and on-time metrics, exception trends and root causes, carrier performance comparisons, cost analysis for shipping expenses, and operational efficiency metrics.

                Many reports can be exported to CSV or PDF format for further analysis, board presentations, or sharing with stakeholders. Regular reporting helps you track progress toward operational goals and justify resource investments.

                Security and Compliance:
                As an administrator, you're responsible for security and compliance aspects of the logistics system. Ensure user access follows the principle of least privilege - users should only have access to features they need for their role. Regularly review user activity logs for suspicious behavior. Ensure shipping documents and customer data are handled in compliance with privacy regulations. Maintain backup and disaster recovery procedures. Keep the system and its integrations up to date with security patches.

                Planning and Continuous Improvement:
                Use the data and insights from SLMS to drive continuous improvement in your logistics operations. Identify bottlenecks in order fulfillment and work to eliminate them. Analyze which carriers provide the best combination of cost and performance. Review exception patterns to identify root causes and implement preventive measures. Gather feedback from warehouse staff, drivers, and customer service representatives about system usability and workflow issues.

                The most effective logistics managers use their systems not just for day-to-day operations but as a source of intelligence for ongoing optimization.

                Best Practices for Administrators:
                Review dashboards daily to stay on top of operations. Respond promptly to escalated issues from customer service or warehouse teams. Keep user permissions up to date as team members change roles. Document any system configuration changes and communicate them to affected users. Regularly back up critical data and test recovery procedures. Stay informed about new carrier services and integration capabilities. Foster communication between warehouse, customer service, and driver teams.

                Getting Help:
                If you encounter system issues or have questions about administrative features, consult the SLMS documentation and help resources. For technical issues beyond your expertise, contact SLMS technical support or your system integrator. The admin console includes a help section with detailed documentation about all administrative functions.

                Next Steps:
                Now that you understand the administrative capabilities of SLMS, explore the admin console thoroughly. Review all dashboards and reports available to you. Familiarize yourself with user management and warehouse configuration interfaces. Set up any necessary system configurations for your organization's needs. Once you're comfortable with all areas of the system, you'll be ready to effectively oversee your entire logistics operation and drive continuous improvement.
                """,
                null
        );
    }

    // ================================
    // ORDER TRACKING DOCUMENTS
    // ================================

    private UpsertDocumentRequest createOrderTrackingDoc() {
        return new UpsertDocumentRequest(
                "How to Track Your Orders in SLMS",
                Set.of("CUSTOMER", "ANONYMOUS", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("landing-page", "customer-portal", "admin-console"),
                Set.of(),
                "en-US",
                """
                Order tracking is one of the most important features of SLMS, providing real-time visibility into where your package is and when it will arrive. Whether you're a customer waiting for a delivery or warehouse staff monitoring fulfillment, understanding how tracking works helps you stay informed about order progress.

                What is Order Tracking:
                Order tracking is the ability to monitor a package's journey from the moment an order is created through final delivery. Every order in SLMS receives a unique tracking number that serves as its identifier throughout its lifecycle. This tracking number is the key to accessing detailed information about the order's current status, location, and history.

                When an order is created, SLMS begins recording tracking events - timestamped records of significant milestones in the order's journey. These events paint a complete picture of what's happening with a package at any given time.

                How to Track an Order:
                There are several ways to track orders in SLMS depending on your role and needs.

                For customers with accounts, the easiest way is to log into your customer portal. Your dashboard displays all your orders with their current status. Click on any order to see its detailed tracking timeline. This timeline shows every event that has occurred with your package, from creation through current status.

                For customers without accounts or anyone who just has a tracking number, the public tracking page on the landing page provides quick access. Simply enter your tracking number in the search box and click Track. You'll see the order's current status and tracking timeline without needing to log in.

                Warehouse staff, customer service representatives, and administrators can access order tracking through the admin console. Search for orders by tracking number, customer name, or other criteria. Once you find the order, its detail page shows comprehensive tracking information plus additional operational details.

                Understanding the Tracking Timeline:
                The tracking timeline is a chronological list of events showing everything that has happened with an order. Each event includes a timestamp, status change, and often a description of what occurred.

                A typical tracking timeline might show order created at a specific date and time with initial status CREATED, status updated to READY_FOR_PICKUP when warehouse staff completed packing, status updated to COLLECTED when the carrier picked up the package from the warehouse, status updated to ASSIGNED_TO_SHIPMENT when the package entered the carrier's system, status updated to IN_TRANSIT as the package moves through the delivery network, and finally status updated to DELIVERED when the driver confirms delivery via QR code scan.

                Each timestamp shows exactly when that event occurred, so you can see how long each stage of the journey took. This transparency helps customers understand their delivery and helps warehouse staff and administrators identify where delays might be happening.

                Tracking Numbers and Format:
                Every SLMS order receives a unique tracking number when it's created. This tracking number follows you throughout the order lifecycle. It appears on shipping labels, in email and SMS notifications, on the customer portal, and in all system interfaces.

                Tracking numbers are designed to be unique and easy to reference. When you receive order confirmation or shipping notifications, save the tracking number so you can check status whenever needed.

                Real-Time Updates:
                SLMS tracking is updated in real-time as events occur. When a driver scans a QR code to confirm delivery, the order status updates to DELIVERED immediately. When warehouse staff updates an order status, it's reflected instantly in all interfaces. This real-time updating ensures everyone always has the most current information about package location and status.

                Automatic Notifications:
                In addition to manually checking tracking, SLMS sends automatic notifications when significant tracking events occur. Customers receive email or SMS messages when their order is created and confirmed, when the package ships from the warehouse, when the package is out for delivery, and when delivery is successfully completed.

                These notifications include the tracking number and current status, so customers can stay informed without constantly checking the tracking page. The notification system is configurable - customers can choose their preferred communication channel and which events they want to be notified about.

                Estimated Delivery Dates:
                Based on the shipping method and destination, SLMS may provide an estimated delivery date. This estimate appears on the tracking page and helps customers plan for their package arrival. The estimate is calculated using carrier delivery time windows and the order's current status.

                While estimates are generally accurate, unexpected delays can occur due to weather, carrier capacity issues, or address problems. The tracking timeline will reflect any delays with updated timestamps showing where the package actually is in its journey.

                Tracking for Different Order Types:
                SLMS handles tracking for various order types. Standard domestic shipments typically move through all status stages in sequence. International shipments may have additional customs clearance events. Express or expedited shipments may move through stages more quickly with shortened time windows.

                Regardless of order type, the tracking system works the same way - providing visibility into current status and full event history.

                Troubleshooting Tracking Issues:
                Sometimes tracking may not update as expected. Common scenarios include tracking showing no movement for several days, delivery confirmation showing but package not received, or tracking number not found in the system.

                If tracking seems stuck or incorrect, first verify you're using the correct tracking number with no typos. Check that enough time has passed - sometimes there's a delay between physical package movement and system updates, especially during carrier handoff. Look at the tracking timeline details to see the most recent event.

                If tracking problems persist, customers should contact customer service. Customer service representatives can investigate tracking issues, check with carriers for updates, and resolve discrepancies between tracking and actual package location.

                Privacy and Tracking:
                Tracking information is tied to orders and customer accounts. Customers can only track their own orders through the customer portal. The public tracking page requires the exact tracking number - you can't browse or search for orders without the tracking number. This protects customer privacy while still allowing convenient tracking access.

                Warehouse staff and customer service representatives can view tracking for all orders as part of their operational responsibilities. Administrators have full visibility into all order tracking.

                Tracking After Delivery:
                Once an order is delivered, its tracking timeline remains accessible. Customers can view past order tracking through their order history. This is useful for recordkeeping, verifying delivery dates for claims or returns, or providing delivery proof for billing or reimbursement.

                The system maintains complete tracking history for all orders, even after they're delivered and closed.

                Using Tracking to Resolve Issues:
                The tracking timeline is invaluable when delivery issues arise. If a package shows as delivered but the customer didn't receive it, the delivery timestamp helps pinpoint exactly when the driver scanned the QR code. This information is crucial for investigating potential misdelivery or theft.

                If a package is delayed, the tracking timeline shows where the delay occurred - whether at the warehouse during processing, during carrier transit, or at the final delivery stage. This helps customer service representatives determine the appropriate resolution approach.

                Best Practices for Tracking:
                Save your tracking number immediately when you receive it in order confirmation or shipping notifications. Check tracking if you're expecting a delivery and want to know when it will arrive. Review the full tracking timeline if you have questions about delays or unexpected status. Contact customer service if tracking shows problems or doesn't match your experience. Use tracking history for recordkeeping and proof of delivery when needed.

                Tracking is designed to give you complete visibility and peace of mind about your shipments. Whether you're a customer waiting for a package, warehouse staff managing fulfillment, or customer service resolving issues, the tracking system provides the information you need to stay informed and make decisions.
                """,
                null
        );
    }

    private UpsertDocumentRequest createOrderStatusesDoc() {
        return new UpsertDocumentRequest(
                "Understanding Order Statuses in SLMS",
                Set.of("CUSTOMER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "DRIVER", "ADMIN", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal", "admin-console", "driver-pwa"),
                Set.of(),
                "en-US",
                """
                Order statuses are the backbone of the SLMS tracking system, providing a standardized way to communicate where a package is in its journey from creation to delivery. Understanding what each status means helps you know what's happening with your order and what to expect next.

                The Order Status Lifecycle:
                Orders in SLMS progress through a defined sequence of statuses that represent different stages of fulfillment and delivery. While not every order goes through every possible status, most follow a standard path. Here's what each status means and what happens at each stage.

                CREATED Status:
                This is the initial status assigned when an order is first entered into SLMS. When you see CREATED status, it means warehouse staff have recorded the order details including customer information, delivery address, and package specifications. The order is in the system and queued for processing, but physical fulfillment hasn't started yet.

                At this stage, the order exists in the database and has been assigned a tracking number, but the package hasn't been picked, packed, or prepared for shipping. For customers, CREATED status means your order has been received and confirmed, and you should receive an order confirmation email with your tracking number.

                Warehouse staff typically work through CREATED orders in sequence, moving them to the next stage as they begin physical fulfillment processes.

                READY_FOR_PICKUP Status:
                When an order moves to READY_FOR_PICKUP, it means warehouse staff have completed all fulfillment activities. The package has been picked from inventory, packed securely, and the shipping label has been attached. The package is physically staged in the warehouse's pickup area waiting for carrier collection.

                At this stage, the order is ready to leave the warehouse. It's just waiting for the scheduled carrier pickup or driver assignment. For customers, READY_FOR_PICKUP means your package is prepared and will be picked up by the carrier soon, typically within the same business day.

                This status is important because it signals the transition from warehouse responsibility to carrier responsibility. Once a package reaches READY_FOR_PICKUP, the fulfillment work is complete and the focus shifts to transportation.

                COLLECTED Status:
                COLLECTED status indicates the carrier or driver has physically taken possession of the package from the warehouse. This is confirmed when warehouse staff updates the status upon carrier pickup, or when the carrier's systems sync with SLMS to confirm receipt.

                At this point, the package has left your warehouse and is now in the carrier's network. For customers, COLLECTED means your package is no longer at the origin location and has begun its journey to you. You should expect to see movement in the carrier's tracking system within a few hours.

                COLLECTED status often triggers the handoff to external carrier tracking systems. The carrier may provide additional tracking events as the package moves through their network, which may appear in SLMS tracking if carrier integration is active.

                ASSIGNED_TO_SHIPMENT Status:
                This status indicates the package has been assigned to a specific shipment or delivery route. In carrier systems, this means the package has been sorted and allocated to a particular transport vehicle or delivery route. For final-mile delivery, it might mean the package has been assigned to a specific driver.

                ASSIGNED_TO_SHIPMENT signals that logistical planning for delivery is complete. The carrier knows exactly how and when this package will be transported to its destination. For customers, this status means your package is part of an active delivery plan and is moving through the fulfillment network.

                Not all orders will show ASSIGNED_TO_SHIPMENT as a distinct status - some may move directly from COLLECTED to IN_TRANSIT depending on how quickly carrier processing occurs.

                IN_TRANSIT Status:
                IN_TRANSIT means your package is actively moving through the delivery network toward its destination. It might be on a truck, plane, or train traveling between distribution centers, or it might be on a local delivery vehicle heading to the final destination.

                This is typically the longest status period for most orders, as packages travel from the origin warehouse through potentially multiple carrier facilities to the destination area. For customers, IN_TRANSIT means your package is on its way and making progress toward delivery.

                During IN_TRANSIT, you might see additional tracking events from the carrier showing arrivals and departures from various facilities. These events help show that your package is actively moving and hasn't been lost or delayed.

                OUT_FOR_DELIVERY Status (if applicable):
                Some implementations of SLMS may include an OUT_FOR_DELIVERY status indicating the package is on a delivery vehicle and the driver will attempt delivery today. This status typically appears on the day of expected delivery and means your package should arrive within business hours.

                For customers, OUT_FOR_DELIVERY is an important status because it means you should be available to receive the package or ensure someone is at the delivery location. If you have delivery instructions, this is when they'll be followed.

                DELIVERED Status:
                DELIVERED is the successful completion status indicating the package has been handed to the recipient or left at the delivery location. In SLMS, delivery is confirmed when the driver scans the QR code on the package. This creates a timestamped delivery record that immediately updates the order status to DELIVERED.

                When you see DELIVERED status, the order lifecycle is complete. The package has reached its destination and the delivery has been confirmed. Customers receive a delivery confirmation notification when this status is set, and they can access proof of delivery information including the exact delivery timestamp.

                DELIVERED status closes the active tracking for an order, though the full tracking history remains accessible through order history.

                CANCELLED Status:
                Sometimes orders need to be cancelled before completion. CANCELLED status indicates the order was terminated before delivery. This might happen due to customer request for cancellation, inability to fulfill the order due to inventory issues, address problems that couldn't be resolved, or business decision to cancel.

                CANCELLED orders don't progress through the normal status sequence. Once cancelled, they typically don't move to any other status. The tracking timeline will show when and why the order was cancelled.

                For customers, CANCELLED means the order won't be delivered and any charges should be refunded according to business policies.

                Status Transitions and Sequence:
                Most orders follow this typical sequence: CREATED → READY_FOR_PICKUP → COLLECTED → ASSIGNED_TO_SHIPMENT → IN_TRANSIT → DELIVERED.

                However, not every order includes every status. Some might skip ASSIGNED_TO_SHIPMENT. Express deliveries might move through statuses much more quickly. International shipments might have additional statuses for customs processing.

                Orders generally can't move backwards in status - they progress forward through the lifecycle. If an issue occurs, it's handled through the delivery exception system rather than reverting to a previous status.

                How Long Orders Stay in Each Status:
                The time spent in each status varies based on shipping method, distance, and operational factors. CREATED might last a few hours to a day as the warehouse processes orders. READY_FOR_PICKUP typically lasts a few hours until the next carrier pickup. COLLECTED to ASSIGNED_TO_SHIPMENT might happen within hours. IN_TRANSIT duration depends on distance and shipping method - might be one day for local delivery or several days for cross-country or international shipments.

                If an order seems stuck in one status longer than expected, it may indicate a delay or issue that should be investigated.

                Checking Order Status:
                You can check current order status through the tracking page by entering your tracking number, through the customer portal dashboard which shows status for all your orders, or by reviewing status information in email or SMS notifications.

                The status appears prominently on tracking pages and order details, making it easy to quickly understand where your package is.

                What to Do If Status Seems Wrong:
                If your order's status doesn't match reality - for example, it shows DELIVERED but you didn't receive the package - check the tracking timeline for the exact delivery timestamp and any additional details, verify the delivery address in the order to ensure it wasn't sent to the wrong location, contact customer service to investigate the discrepancy, and report a delivery exception if the status is incorrect.

                Status accuracy is important for customer satisfaction and operational visibility, so SLMS works to ensure statuses reflect reality. Drivers confirming delivery via QR code scanning helps maintain accuracy for the critical DELIVERED status.

                Status Notifications:
                SLMS sends automatic notifications when orders reach certain statuses. Typically you'll be notified when status changes to READY_FOR_PICKUP or COLLECTED (shipping confirmation), IN_TRANSIT or OUT_FOR_DELIVERY (package on its way), and DELIVERED (delivery confirmation).

                These notifications keep you informed without requiring constant manual tracking checks.

                Understanding order statuses helps you know what's happening with your package, set appropriate expectations for delivery timing, and recognize when something might be wrong that needs investigation. The status system provides a common language for customers, warehouse staff, drivers, and administrators to communicate about order progress.
                """,
                null
        );
    }

    private UpsertDocumentRequest createOrderHistoryDoc() {
        return new UpsertDocumentRequest(
                "Viewing and Using Order History",
                Set.of("CUSTOMER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("customer-portal", "admin-console"),
                Set.of(),
                "en-US",
                """
                Order history is a powerful feature in SLMS that provides access to complete records of all past orders. Whether you're a customer reviewing your delivery patterns, warehouse staff analyzing fulfillment performance, or customer service investigating an issue, order history gives you the information you need.

                What is Order History:
                Order history is a complete, searchable database of all orders that have been processed through SLMS. For customers, order history includes all orders associated with your account. For warehouse staff and administrators, order history includes all orders processed through the system or specific warehouses.

                Every order, regardless of its final status (delivered, cancelled, or with issues), is preserved in order history. This creates a permanent record for reference, analysis, and customer service.

                Accessing Order History:
                Customers can access their order history by logging into the customer portal and navigating to the Order History or Past Orders section. This displays a list of all your previous orders with key information like tracking number, order date, destination, final status, and delivery date if applicable.

                Warehouse staff and administrators access order history through the admin console's order management section. They can view all orders system-wide or filter to specific warehouses, date ranges, or customers.

                Viewing Order Details from History:
                Click on any order in your history to see its complete details. This includes all the original order information like customer and destination address, package specifications, shipping method and carrier, and all tracking events and status changes in chronological order.

                You'll also see associated documents like shipping labels and invoices, delivery confirmation details including timestamp, notes and comments added during fulfillment, and any delivery exceptions that were reported and how they were resolved.

                This comprehensive view gives you the full story of what happened with each order from creation through completion.

                Searching and Filtering Order History:
                As your order history grows, finding specific orders becomes important. SLMS provides several ways to search and filter history.

                You can search by tracking number to pull up a specific order immediately. Search by date range to see all orders from a particular time period. Filter by status to show only delivered orders, only cancelled orders, or orders with specific statuses. Filter by destination address or city. Search by customer name or email for warehouse staff and customer service.

                Combining filters helps you find exactly what you're looking for. For example, you might filter for all delivered orders to a specific city in the last three months.

                Why Order History is Useful:
                Order history serves many practical purposes. For customers, you can verify delivery dates for expense reports or reimbursement claims. Review past deliveries to the same address for comparison. Download shipping documents you might have lost. Check if an item was previously ordered and when it was delivered. Provide delivery proof for disputes or insurance claims.

                For warehouse staff, order history helps analyze fulfillment performance and identify bottlenecks. Compare processing times across different warehouses. Review how similar orders were handled in the past. Training new staff by showing examples of completed orders. Audit trail for quality control and compliance.

                For customer service representatives, order history is essential for resolving customer inquiries. View a customer's complete order history to understand their experience. Verify claims about past deliveries or issues. See how previous exceptions were resolved for similar situations. Provide accurate information about past orders when customers call with questions.

                Tracking Timeline in Order History:
                Even after an order is delivered, its complete tracking timeline remains accessible through order history. You can see every status change, who made the change, and exactly when it occurred. This historical tracking data is valuable for understanding delivery patterns, investigating issues, and confirming delivery details.

                For example, if a customer disputes a delivery date months later, you can pull up the order history and show the exact timestamp when the driver scanned the QR code to confirm delivery.

                Documents in Order History:
                All documents associated with orders are preserved in order history. Customers can download shipping labels, commercial invoices, packing lists, and proof of delivery records even months or years after the order was delivered.

                This is particularly useful for international shipments where customs documentation might be needed for records, or for businesses that need historical shipping documents for accounting and auditing purposes.

                Exception History:
                If an order had delivery exceptions reported and resolved, the complete exception history appears in the order details. You can see what issue was reported, who was assigned to resolve it, what actions were taken, and how it was ultimately resolved.

                This exception history is valuable for identifying patterns in delivery issues, training customer service staff on resolution approaches, and demonstrating to customers how their issues were handled.

                Order History Reports and Analytics:
                Beyond viewing individual orders, order history can be used for reporting and analytics. Administrators and logistics managers can analyze order volume trends over time, average delivery times by shipping method or destination, fulfillment efficiency and warehouse performance, carrier performance and delivery success rates, and patterns in delivery exceptions.

                These insights help optimize operations, negotiate better carrier rates, and improve customer experience.

                Privacy and Access Control:
                Order history respects role-based access control. Customers can only view their own order history - they cannot see other customers' orders. Warehouse staff can view orders processed through their warehouse or all orders depending on their role. Customer service representatives can view any customer's order history to provide support. Administrators have full visibility into all order history.

                This ensures customer privacy while giving operational staff the access they need to do their jobs.

                Order History Retention:
                SLMS maintains order history indefinitely (or according to configured retention policies). Even very old orders remain accessible for reference. This long-term retention supports compliance requirements, historical analysis, and customer service for questions about orders placed long ago.

                Some organizations may configure retention policies that archive very old orders to separate storage while keeping recent orders in the primary system for performance reasons.

                Exporting Order History:
                Administrators and warehouse staff can often export order history data to CSV or other formats for analysis in external tools. This is useful for creating custom reports, importing into accounting systems, or sharing with partners and carriers.

                Customers might also be able to export their own order history for personal recordkeeping.

                Using Order History for Returns and Claims:
                When processing returns or insurance claims, order history provides the documentation needed. You can pull up the original order with all its details, download shipping documents and delivery confirmation, show the tracking timeline proving when and how delivery occurred, and access any exception reports related to damage or issues.

                This complete documentation makes it easier to process legitimate returns and claims while preventing fraudulent ones.

                Best Practices for Using Order History:
                Reference order history when you have questions about past deliveries before contacting customer service. Use search and filtering to quickly find specific orders rather than scrolling through pages of results. Download and save important shipping documents when you receive them rather than relying only on order history access. Review your order history periodically to verify all orders are accounted for. For businesses, reconcile order history with invoices and billing statements.

                Order history is designed to be your permanent, reliable record of all shipment activity in SLMS. Whether you need it for a customer service inquiry, business analysis, compliance documentation, or personal recordkeeping, the information you need is preserved and accessible.
                """,
                null
        );
    }

    private UpsertDocumentRequest createOrderCreationDoc() {
        return new UpsertDocumentRequest(
                "Creating Orders in SLMS - Warehouse Staff Guide",
                Set.of("WAREHOUSE_STAFF", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Creating orders in SLMS is a fundamental warehouse operation that begins the fulfillment process. When done correctly, order creation ensures packages have all the information needed for successful delivery. This guide covers everything warehouse staff need to know about creating orders in the system.

                When to Create Orders:
                Orders are typically created when new shipment requests come in from customers through sales channels, e-commerce platforms, or direct customer requests. The order creation process in SLMS formalizes these requests into the system so they can be tracked and fulfilled.

                Some organizations integrate their e-commerce platforms with SLMS so orders are created automatically. Others require warehouse staff to manually create orders based on incoming requests. Regardless of the source, proper order creation is the first step to successful fulfillment.

                Accessing the Order Creation Interface:
                Log into the SLMS admin console with your warehouse staff credentials. Navigate to the Orders section and look for the "Create New Order" or "Add Order" button. This opens the order creation form where you'll enter all necessary information.

                Required Order Information:
                Creating an order requires several pieces of information. Make sure you have all details before starting to avoid incomplete orders.

                Customer Information - You'll need the customer's full name exactly as it should appear on shipping documents, email address for notifications and confirmations, phone number for carrier contact if needed, and customer account ID if applicable.

                Destination Address - The shipping destination must be complete and accurate. Enter the street address including apartment or suite numbers, city name, state or province, postal or ZIP code, and country for international shipments. Address accuracy is critical - incorrect addresses cause delivery failures and costly returns. Double-check the address before submitting the order.

                Package Specifications - You need to specify package details for carrier rating and label generation including package dimensions (length, width, height), package weight, package value for insurance purposes, and contents description. These details affect shipping costs and carrier service level, so accuracy matters.

                Shipping Service Level - Select the appropriate shipping method such as standard ground delivery, expedited 2-day or overnight service, international shipping, or special handling requirements. The service level affects cost and delivery timeframe, so choose based on customer needs and expectations.

                Origin Warehouse - Specify which warehouse will fulfill this order. This is particularly important for organizations with multiple warehouse locations. The origin warehouse affects shipping cost, delivery time, and inventory availability.

                Special Instructions - If there are special delivery instructions like "leave at back door," "signature required," or "fragile - handle with care," enter them in the special instructions field. These instructions help ensure the package is handled and delivered appropriately.

                Step-by-Step Order Creation Process:
                Click the "Create New Order" button to open the order form. Fill in all required fields, starting with customer information. Enter the complete and accurate destination address. Specify package dimensions, weight, and value. Select the shipping service level. Choose the origin warehouse if you have multiple locations. Add any special instructions or handling notes. Review all entered information carefully for accuracy. Click "Submit" or "Create Order" to finalize.

                Once submitted, SLMS will assign a unique tracking number to the order, set its initial status to CREATED, generate an order confirmation for the customer, and add the order to the fulfillment queue.

                Tracking Numbers and Order Confirmation:
                After successful order creation, SLMS displays the assigned tracking number. Make note of this tracking number - the customer will use it to track their shipment. The system also sends an order confirmation email to the customer's provided email address, including the tracking number and order details.

                If you're creating orders based on customer requests received via phone or in person, provide the tracking number to the customer immediately so they can monitor their order.

                Validating Addresses During Order Creation:
                SLMS may include address validation features that check destination addresses against carrier databases. If address validation is enabled, the system will flag potentially invalid addresses during order creation.

                If you receive an address validation warning, review the entered address carefully. The system might suggest corrections - for example, correcting a misspelled street name or updating an incorrect ZIP code. You can usually accept the suggested correction or override it if you're certain the original address is correct.

                Address validation helps prevent delivery failures due to address errors, saving time and money on returned packages.

                Calculating Shipping Costs:
                Some SLMS configurations automatically calculate shipping costs during order creation using integrated carrier APIs. The system sends the package specifications and destination to carriers, receives rate quotes, and can automatically select the most cost-effective carrier for the chosen service level.

                If shipping cost calculation is enabled, you'll see estimated costs before finalizing the order. This helps with pricing decisions and customer quotes.

                Order Creation Errors and Validation:
                SLMS validates order information as you enter it. Common validation errors include missing required fields like customer email or destination address, invalid address formats, package dimensions or weight outside acceptable ranges, and invalid service level selections.

                If you encounter validation errors, the system will highlight the problematic fields and explain what needs to be corrected. Fix the errors and resubmit.

                If you're having trouble creating an order due to validation errors you can't resolve, contact your warehouse manager or system administrator for assistance.

                Bulk Order Creation:
                For high-volume operations, SLMS may support bulk order creation through file import. Instead of manually entering each order, you can upload a CSV or Excel file containing multiple orders with all required information.

                Bulk import requires the file to follow a specific format with columns for each required field. Your system administrator can provide the template format. After uploading, SLMS validates all orders and creates them in batch, dramatically speeding up the process for large order volumes.

                Creating International Orders:
                International orders require additional information beyond standard domestic shipments. When creating international orders, ensure you include complete destination country information, detailed customs declarations describing package contents and values, harmonized tariff codes if required, and commercial invoice information.

                International orders may also require specific carrier selection that handles cross-border shipping. Ensure the selected carrier and service level support international delivery to the destination country.

                Order Creation Best Practices:
                Always double-check addresses before submitting - address errors are a common cause of delivery failures. Verify package weight and dimensions are accurate to prevent carrier surcharges for incorrect specifications. Choose service levels that match customer expectations and delivery timeframes. Include special instructions when relevant to help drivers and carriers handle packages properly. Create orders promptly when requests come in to avoid fulfillment delays. Keep customer contact information accurate for delivery coordination and notifications.

                What Happens After Order Creation:
                Once you create an order, it enters the fulfillment workflow with CREATED status. The order appears in the warehouse work queue for picking and packing. Other warehouse staff can see the order and begin processing it. The customer receives an order confirmation and can begin tracking.

                The order will progress through statuses as you and your team fulfill it: updating to READY_FOR_PICKUP once packed, moving to COLLECTED when the carrier picks it up, advancing through delivery stages until final delivery confirmation.

                Editing Orders After Creation:
                Depending on system configuration, you may be able to edit certain order fields after creation if the order hasn't been shipped yet. Common editable fields include customer contact information, special delivery instructions, and package value or contents description.

                Address changes after creation require careful handling. If a customer requests an address change after you've created the order but before shipping, you may be able to update the destination. However, once the carrier has picked up the package (COLLECTED status or later), address changes require carrier coordination through customer service.

                Cancelling Orders:
                If an order needs to be cancelled before shipping, you can usually change its status to CANCELLED through the order detail page. Add a cancellation reason in the notes. Cancelled orders stop the fulfillment process and trigger customer notification.

                Only cancel orders when appropriate - such as customer request, inventory unavailability, or inability to fulfill as specified.

                Getting Help with Order Creation:
                If you encounter issues or have questions about creating orders, consult your warehouse manager for guidance on specific scenarios, check the SLMS help documentation for order creation instructions, contact system administrators for technical issues or access problems, or use the SLMS chatbot for quick answers to common questions.

                Proper order creation sets the foundation for successful fulfillment and delivery. Taking time to enter accurate, complete information prevents downstream problems and ensures customers receive their packages on time and as expected.
                """,
                null
        );
    }

    private UpsertDocumentRequest createOrderLifecycleDoc() {
        return new UpsertDocumentRequest(
                "Understanding the Complete Order Lifecycle in SLMS",
                Set.of("WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                The order lifecycle in SLMS represents the complete journey of a package from the moment an order is created through final delivery. Understanding this lifecycle helps warehouse staff, customer service representatives, and administrators know what happens at each stage and how to manage orders effectively.

                Overview of the Order Lifecycle:
                Every order in SLMS follows a defined lifecycle with specific stages and status transitions. While individual orders may have variations based on shipping method, destination, or issues that arise, the standard lifecycle provides a framework for managing all orders consistently.

                The typical order lifecycle consists of these major phases: order creation and entry into the system, warehouse fulfillment and preparation, carrier pickup and handoff, transportation and delivery, and delivery confirmation and completion.

                Let's explore each phase in detail.

                Phase 1: Order Creation:
                The lifecycle begins when an order is created in SLMS. This might happen through warehouse staff manually entering order details, automatic creation via e-commerce integration, or bulk import of multiple orders.

                During order creation, the system captures all necessary information including customer details, destination address, package specifications, and shipping requirements. Once creation is complete, the order receives a unique tracking number and enters CREATED status.

                At this point, the order exists in the system but hasn't been physically processed yet. It sits in the warehouse work queue waiting for fulfillment to begin. The customer receives an order confirmation notification with their tracking number.

                Phase 2: Warehouse Fulfillment:
                This is where the physical work of order fulfillment happens. Warehouse staff process CREATED orders by picking items from inventory, packing them securely in appropriate packaging, generating and printing shipping labels, attaching labels and any required documents to packages, and conducting quality checks to ensure correctness.

                As warehouse staff complete these activities, they can update the order status to reflect progress. Some implementations might have intermediate statuses like PICKING, PACKING, or QUALITY_CHECK. Others move directly from CREATED to READY_FOR_PICKUP once all fulfillment is complete.

                READY_FOR_PICKUP status signals that warehouse work is done. The package is complete, labeled, and staged in the designated pickup area waiting for carrier collection.

                This phase is critical because quality fulfillment work here prevents issues later. Accurate picking ensures the right items are shipped. Proper packing prevents damage during transit. Correct labeling ensures packages reach the right destination.

                Phase 3: Carrier Pickup and Handoff:
                Once packages reach READY_FOR_PICKUP status, they're waiting for carrier collection. Carriers typically make scheduled pickups from warehouses - perhaps daily, multiple times per day, or on specific schedules based on volume.

                When the carrier arrives, warehouse staff verify which packages are being picked up and confirm the carrier has taken possession. At this point, the order status updates to COLLECTED. This status change triggers the handoff from warehouse responsibility to carrier responsibility.

                The package is now in the carrier's logistics network. Depending on carrier integration, SLMS might start receiving tracking events from the carrier's systems showing package progress through their facilities.

                Phase 4: Assignment to Shipment:
                After the carrier collects packages, they sort them and assign them to specific shipments or delivery routes. This is when ASSIGNED_TO_SHIPMENT status is set.

                In carrier operations, this means the package has gone through sorting facilities, been scanned into their tracking system, and allocated to a specific transport method - perhaps a truck route, flight, or train shipment depending on the shipping method and destination.

                For final-mile delivery, ASSIGNED_TO_SHIPMENT might mean the package is on a local delivery truck assigned to a specific driver's route.

                This phase represents the carrier's logistical planning. They know exactly how and when this package will move toward its destination.

                Phase 5: In-Transit Movement:
                The IN_TRANSIT status covers the transportation phase where packages physically move from origin to destination. This is often the longest phase, especially for long-distance or international shipments.

                During IN_TRANSIT, packages might pass through multiple carrier facilities including regional distribution centers, sorting hubs, and local delivery facilities. Each facility scan creates a tracking event showing the package's progress.

                For customers, IN_TRANSIT means their package is actively moving toward them. For operational staff, this is a waiting phase - the package is in carrier hands and progressing normally.

                If a package sits in IN_TRANSIT status significantly longer than expected for its shipping method and destination, it might indicate a delay that requires investigation.

                Phase 6: Out for Delivery (if applicable):
                Some carriers and implementations include an OUT_FOR_DELIVERY status indicating the package is on a delivery vehicle for final delivery today. This status typically appears in the morning when drivers load their vehicles and scan packages.

                OUT_FOR_DELIVERY tells customers to expect delivery today and to be available or ensure delivery location access. It's the final stage before delivery completion.

                Phase 7: Delivery Confirmation:
                The lifecycle completes when the package reaches the customer and delivery is confirmed. In SLMS, this happens when the driver scans the QR code on the package using the driver PWA.

                The QR code scan creates a timestamped delivery confirmation that immediately updates order status to DELIVERED. This triggers customer notification of successful delivery and marks the order as complete.

                The DELIVERED status signifies successful lifecycle completion. The package has reached its destination, the customer has received it, and the delivery has been confirmed. From a logistics perspective, this order is now closed.

                Alternative Outcomes: Cancellation:
                Not all orders complete the standard lifecycle. Sometimes orders are cancelled before delivery. Cancellation can happen during early stages (CREATED or READY_FOR_PICKUP) due to customer request, inventory unavailability, or fulfillment issues.

                When an order is cancelled, it receives CANCELLED status and stops progressing through the lifecycle. The customer is notified and any shipping charges are typically refunded.

                Cancelled orders don't reach delivery, but they remain in order history for recordkeeping and analysis.

                Alternative Outcomes: Delivery Exceptions:
                Sometimes problems occur during the lifecycle that prevent normal progression. Common issues include damaged packages during transit, incorrect address causing delivery failure, customer unavailable for delivery, or package lost in carrier network.

                When these issues arise, the order might not progress normally through statuses. Instead, a delivery exception is created to track and resolve the issue. The order status might remain in IN_TRANSIT or another status while the exception is investigated and resolved.

                Customer service representatives work to resolve exceptions through actions like rescheduling delivery, updating addresses, initiating returns, or arranging replacements. Once resolved, the exception is closed and the order either completes delivery or is cancelled depending on the resolution.

                Lifecycle Timing and Duration:
                The total lifecycle duration depends on many factors including shipping method (ground vs. express), destination distance, carrier performance, and any delays or issues.

                A typical local ground shipment might complete its lifecycle in 2-3 days from creation to delivery. Cross-country ground shipments might take 5-7 days. Express shipments might complete in 1-2 days. International shipments can take 1-3 weeks depending on customs and distance.

                Within the lifecycle, different phases take varying amounts of time. Warehouse fulfillment might take hours to a day. Carrier pickup happens on scheduled times. IN_TRANSIT duration varies by distance and method. Final delivery typically occurs during business hours.

                Tracking the Lifecycle:
                The order tracking timeline shows the complete lifecycle progression. Each status change appears with a timestamp, creating a chronological record of the order's journey.

                Customers, warehouse staff, and customer service representatives can view this timeline to understand where an order is in its lifecycle and identify any unusual delays or issues.

                Lifecycle Management Responsibilities:
                Different roles manage different lifecycle phases. Warehouse staff own the creation and fulfillment phases through READY_FOR_PICKUP. Carriers own pickup through delivery phases. Drivers confirm final delivery via QR code scanning. Customer service representatives intervene when issues arise to get orders back on track.

                Administrators oversee the entire lifecycle, monitoring for systemic issues and optimizing processes.

                Optimizing the Lifecycle:
                Efficient lifecycle management improves customer satisfaction and reduces costs. Key optimization areas include speeding warehouse fulfillment to reduce time in CREATED and READY_FOR_PICKUP statuses, ensuring reliable carrier performance to minimize IN_TRANSIT delays, accurate delivery confirmation to prevent exceptions, and proactive exception resolution to recover from issues quickly.

                By understanding and optimizing each lifecycle phase, SLMS operations deliver better customer experience and more efficient logistics.

                Lifecycle Reporting and Analytics:
                SLMS can generate reports analyzing lifecycle performance such as average time from creation to delivery, time spent in each lifecycle phase, percentage of orders completing normally vs. experiencing exceptions, and lifecycle performance by warehouse, carrier, or destination.

                These metrics help identify bottlenecks, measure performance against goals, and justify process improvement investments.

                Understanding the order lifecycle gives you insight into the complex orchestration required to move packages from warehouse to customer. Each phase has specific activities, responsibilities, and expected outcomes that together enable successful delivery.
                """,
                null
        );
    }

    private UpsertDocumentRequest createShippingDocumentsDoc() {
        return new UpsertDocumentRequest(
                "Managing Shipping Documents in SLMS",
                Set.of("CUSTOMER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("customer-portal", "admin-console"),
                Set.of(),
                "en-US",
                """
                Shipping documents are essential records that accompany orders throughout their lifecycle. SLMS provides comprehensive document management capabilities that help warehouse staff, customers, and administrators access the documentation they need when they need it.

                What Are Shipping Documents:
                Shipping documents are files associated with orders that provide important information about the shipment. These documents serve various purposes from enabling carrier delivery to providing customs information to offering proof of delivery.

                Common shipping documents in SLMS include shipping labels that carriers use to route packages and that contain tracking barcodes, commercial invoices for international shipments that declare package contents and value for customs, packing lists that detail items included in the package, proof of delivery records that confirm successful delivery with timestamps, customs forms required for international shipments, and special handling documentation for hazardous materials or other special requirements.

                Why Shipping Documents Matter:
                Shipping documents serve critical functions. Shipping labels are required for carriers to accept and deliver packages - without a proper label, the package cannot be shipped. Commercial invoices are legally required for international shipments to clear customs. Proof of delivery documents protect both customers and logistics providers in disputes about whether delivery occurred. Packing lists help customers verify they received the correct items.

                Proper document management ensures compliance with regulations, enables smooth carrier operations, provides recordkeeping for accounting and auditing, and supports customer service when questions or issues arise.

                Shipping Labels:
                The shipping label is the most important document for any order. It contains the destination address, return address, tracking barcode, carrier service level, and special handling instructions.

                In SLMS, shipping labels are typically generated through carrier API integrations. When warehouse staff prepare an order for shipping, they can trigger label generation. SLMS sends the order details to the carrier API, receives a PDF label, automatically uploads the label to the order's documents, and makes it available for printing.

                Warehouse staff print the label and affix it to the package. The barcode on the label is what carriers scan throughout the delivery process to track the package.

                For customers, the shipping label might be available for download from the customer portal, though typically customers don't need to access it unless they're making a return or investigating an issue.

                Commercial Invoices for International Shipments:
                International shipments require commercial invoices that declare package contents, values, and other information for customs purposes. These invoices must accurately describe what's being shipped and its value to comply with import/export regulations.

                SLMS can generate commercial invoices automatically based on order information, or warehouse staff can upload manually created invoices. The commercial invoice includes detailed item descriptions with harmonized tariff codes, declared values in appropriate currency, shipper and receiver information, and reason for export (sale, gift, sample, etc.).

                Commercial invoices must accompany international packages - they're typically attached to the outside of the package or included inside. Customs officials use these invoices to assess duties and taxes and to ensure compliance with import regulations.

                For customers receiving international shipments, having access to the commercial invoice through SLMS can help if customs questions arise.

                Packing Lists:
                Packing lists detail exactly what items are included in a package. While not always required, packing lists are helpful for customers receiving multi-item orders and for business-to-business shipments where verification of contents is important.

                Warehouse staff can upload packing lists when preparing orders. The packing list might be generated from inventory systems showing which items were picked and packed for this order.

                Customers can download packing lists to verify their order before or after delivery.

                Proof of Delivery:
                Proof of delivery documents confirm that a package was successfully delivered, when it was delivered, and potentially who received it. In SLMS, proof of delivery is created when the driver scans the QR code to confirm delivery. This creates a timestamped record.

                Proof of delivery is valuable for customers who need to verify delivery occurred for expense reimbursement or billing purposes. It's also crucial for resolving disputes - if a customer claims non-delivery but proof of delivery shows successful delivery with a timestamp, this evidence helps resolve the disagreement.

                Proof of delivery documents are automatically created upon delivery confirmation and stored with the order. Customers can download them from their order history.

                Uploading Documents to Orders:
                Warehouse staff and administrators can upload documents to orders at any time during the order lifecycle. To upload a document, open the order detail page in the admin console, navigate to the Documents section, click "Upload Document" or similar button, select the document file from your computer (PDF, PNG, JPEG typically supported), choose the document type from the dropdown (shipping label, invoice, packing list, etc.), optionally add a description, and click Upload.

                The document is now associated with the order and available for download by authorized users.

                Best practices for uploading documents include upload labels before updating order status to READY_FOR_PICKUP, ensure commercial invoices are uploaded for all international orders, use clear file names that indicate what the document is, and verify documents are readable and complete before uploading.

                Downloading Documents:
                Customers can download their shipping documents through the customer portal. Navigate to the order detail page and find the Documents section. Click the download button next to any document to save it to your device.

                Common reasons customers download documents include accessing proof of delivery for recordkeeping, obtaining commercial invoices for customs or tax purposes, getting shipping labels for returns, and retrieving packing lists to verify order contents.

                Warehouse staff, customer service, and administrators can download documents through the admin console using the same process.

                Document Storage and Security:
                SLMS stores all shipping documents securely using backend storage systems like MinIO. Documents are associated with specific orders and protected by access controls.

                Customers can only access documents for their own orders. Warehouse staff and customer service can access documents for orders they're authorized to manage. Administrators have full document access.

                This security ensures customer privacy while giving operational staff the access they need.

                Document Retention:
                Shipping documents are retained along with order history. Even after orders are delivered and closed, their documents remain accessible. This long-term retention supports compliance requirements (some regulations require keeping shipping documents for years), customer service for questions about old orders, accounting and auditing needs, and legal requirements for recordkeeping.

                Your organization may have specific retention policies that determine how long documents are kept in primary storage versus archived to long-term storage.

                Automatically Generated vs. Manually Uploaded Documents:
                Some documents are generated automatically by SLMS or carrier integrations. Shipping labels are typically auto-generated through carrier APIs. Proof of delivery is auto-generated upon delivery confirmation. Some commercial invoices can be auto-generated from order data.

                Other documents require manual upload by warehouse staff such as custom packing lists, specialized customs forms, and special handling documentation.

                Whether auto-generated or manually uploaded, all documents are managed the same way once they're associated with an order.

                Documents in Exception Resolution:
                When delivery exceptions occur, shipping documents play an important role in investigation and resolution. Customer service representatives review shipping labels to verify addresses, check commercial invoices to confirm declared contents match what customer expected, examine proof of delivery to verify delivery occurred as claimed, and use packing lists to verify items shipped.

                Customers can also attach documents when reporting exceptions - for example, photos of damaged packaging or incorrect items received.

                Missing or Incorrect Documents:
                If an order is missing required documents or documents are incorrect, it can cause problems. Missing shipping labels prevent carrier pickup. Missing commercial invoices cause customs delays for international shipments. Incorrect addresses on labels cause delivery failures.

                If you notice missing or incorrect documents, warehouse staff should upload or correct documents immediately. Customer service can assist customers who need documents that weren't properly uploaded.

                Document Quality and Legibility:
                When uploading documents, ensure they're high quality and legible. Blurry or low-resolution documents may not be useful when needed. Shipping labels must be clear enough for carrier barcode scanners to read. Commercial invoices must be legible for customs officials.

                If you're scanning physical documents to upload, use good scanning settings (300 DPI or higher for important documents). If generating PDFs from digital sources, ensure text is readable.

                Using Documents for Claims and Disputes:
                Shipping documents provide crucial evidence for insurance claims, delivery disputes, customs issues, and billing questions.

                If a package is lost or damaged, shipping documents plus proof of value support insurance claims. If a customer disputes delivery, proof of delivery documents resolve the disagreement. If customs questions arise, commercial invoices provide the needed information.

                Having complete, accurate shipping documents in SLMS ensures you have the evidence needed when issues arise.

                Best Practices for Document Management:
                Upload all required documents promptly during order processing. Verify document accuracy before uploading - correct addresses, values, descriptions. Use appropriate document types when uploading so documents are categorized correctly. Keep documents organized and consistently named. Download and save important documents to your own records as backup. For businesses, reconcile shipping documents with invoices and accounting records.

                Effective document management ensures you have the information you need throughout the order lifecycle and after delivery. Whether you're a warehouse staff member preparing shipments, a customer tracking orders, or a customer service representative resolving issues, shipping documents in SLMS provide the detailed records that make logistics operations run smoothly.
                """,
                null
        );
    }

    // ================================
    // DELIVERY EXCEPTION DOCUMENTS
    // ================================

    private UpsertDocumentRequest createReportDeliveryIssueDoc() {
        return new UpsertDocumentRequest(
                "How to Report a Delivery Issue or Problem",
                Set.of("CUSTOMER"),
                Set.of("customer-portal"),
                Set.of(),
                "en-US",
                """
                Sometimes deliveries don't go as planned. When problems occur with your package, SLMS provides a delivery exception reporting system that helps you report issues and get them resolved quickly. This guide explains how to report delivery problems and what happens after you submit a report.

                What is a Delivery Exception:
                A delivery exception is a reported problem or issue with an order that prevents normal, successful delivery. Exceptions can occur at any point from warehouse shipment through final delivery. Common examples include damaged packages, incorrect items received, packages marked as delivered but not received, delivery to the wrong address, late deliveries beyond estimated dates, and packages lost in transit.

                When you report a delivery exception, it creates a formal record in SLMS that triggers the customer service resolution workflow. This ensures your issue is tracked, assigned to a representative, and resolved systematically.

                When to Report an Exception:
                You should report an exception whenever something goes wrong with your delivery that requires investigation or action. If your package arrives damaged with visible external damage or items inside are broken, report it immediately. If you received the wrong items or quantities that don't match your order, report the discrepancy. If your tracking shows delivered but you didn't receive the package, report non-receipt. If the package was delivered to the wrong address or left in an insecure location against your instructions, report the delivery error. If your package is significantly delayed beyond the estimated delivery date with no tracking updates, report the delay.

                Don't hesitate to report legitimate issues. The exception system exists to help resolve problems and ensure you receive what you ordered in good condition.

                How to Report a Delivery Exception:
                Reporting an exception through the customer portal is straightforward. Log into your customer portal and navigate to your order list. Find the order with the issue and click to open the order detail page. Look for a "Report Issue," "Create Exception," or similar button - usually prominently displayed on the order detail page. Click the button to open the exception reporting form.

                In the form, you'll provide information about the problem. Select the exception type from a dropdown menu - options typically include damaged package, incorrect items, non-receipt, wrong delivery location, late delivery, and other. Provide a detailed description of what happened. Be specific and thorough - for example, instead of just "damaged," describe "box was crushed on one corner, contents inside were broken." If relevant and possible, attach photos. Pictures of damaged packaging, incorrect items, or delivery location issues can significantly help with investigation and resolution.

                Review your information and click Submit to create the exception. You'll receive a confirmation that your exception has been reported and a customer service representative will be assigned to investigate.

                What Information to Include:
                The more detailed and specific your exception report, the faster it can be resolved. Good exception reports include when you discovered the issue (immediately upon delivery, when you opened the package, etc.), what specifically is wrong (exactly what damage occurred, which items are incorrect, where the package was delivered instead of the correct location), photos or evidence if applicable, and what resolution you're seeking (replacement, refund, redelivery, etc.).

                For damage claims, photos are particularly valuable. Take pictures showing the damaged outer packaging, damaged items inside, all sides of the package if relevant, and any shipping labels or markings that might indicate rough handling.

                For non-receipt claims, note when the tracking shows delivery and where you checked for the package (front door, mailbox, building office, neighbors, etc.).

                Exception Types Explained:
                Understanding exception types helps you select the right category when reporting. Damaged Package means the package or its contents arrived with damage. This could be shipping damage, crushed boxes, broken items, or compromised packaging. Incorrect Items means you received items that don't match your order - wrong products, wrong quantities, or missing items. Non-Receipt means tracking shows delivered but you didn't receive the package. This could indicate misdelivery, theft, or scanning errors. Wrong Delivery Location means the package was delivered to an incorrect address or left in an inappropriate location. Late Delivery means the package arrived significantly after the estimated delivery date. Other covers issues that don't fit standard categories.

                What Happens After You Report:
                Once you submit an exception, several things happen automatically. The system creates an exception record linked to your order with all the information you provided. The exception enters the customer service workflow with OPEN status. A customer service representative is assigned to investigate your issue. You receive a confirmation email that your exception was reported with an exception reference number.

                The assigned representative will begin investigating your issue. This might involve reviewing order and shipping documents, checking tracking information and carrier records, contacting the carrier for additional information, examining photos you provided, and determining the appropriate resolution approach.

                You can check the status of your exception by returning to the order detail page in the customer portal. The exception will appear with its current status and any updates from the customer service team.

                Communication During Exception Resolution:
                Customer service representatives will communicate with you about your exception. You'll receive messages when the representative begins investigating, if they need additional information from you, when they take actions like arranging replacement or refund, and when the issue is resolved.

                Respond promptly to any requests for additional information. If the representative asks for more photos, clarification about what happened, or confirmation of your preferred resolution, quick responses help speed up the process.

                You can also check for updates by logging into the customer portal and viewing the exception details on your order page.

                Resolution Timeframes:
                Exception resolution time varies based on the issue type and complexity. Simple issues like confirming delivery address errors might be resolved in hours. Carrier investigations for lost packages might take several days. Damage claims requiring carrier inspection can take longer. Replacement shipments depend on inventory availability and fulfillment time.

                Customer service will provide an estimated resolution timeline when they begin working on your exception. If resolution takes longer than estimated, they'll update you with the reason and new timeline.

                Possible Resolutions:
                Depending on your issue, possible resolutions include:

                Replacement shipment for incorrect or damaged items. Refund if items can't be replaced or you prefer a refund. Package located and redelivered if the carrier finds a misdelivered or held package. Delivery rescheduled with corrected address or new delivery instructions. Return initiated if you received incorrect items that need to be sent back. Compensation for significant delays or issues affecting high-value shipments.

                The customer service representative will work with you to determine the resolution that best addresses your specific issue.

                Preventing Future Issues:
                While some delivery problems are unavoidable, you can reduce the likelihood of issues. Ensure your delivery address is complete and accurate when ordering. Provide special delivery instructions if needed like gate codes, apartment numbers, or safe drop-off locations. Choose shipping methods appropriate for your item's value and fragility. Be available or arrange for someone to receive packages for high-value or signature-required deliveries. Track your packages and report issues promptly when they occur.

                Multiple Exception Reports:
                If you have multiple orders with issues, report each exception separately on the appropriate order. This ensures each issue is properly tracked and resolved. Don't combine multiple order problems into a single exception report.

                If you have multiple issues with the same order (for example, some items wrong and others damaged), you can include all issues in one exception report. Describe each problem separately so the representative can address all of them.

                Fraud Prevention and Verification:
                SLMS takes exception reports seriously but also has measures to prevent fraudulent claims. Customer service may request verification for certain claims. For non-receipt claims, they might check driver delivery confirmation via QR code, review delivery location photos if available, or contact neighbors or building management. For damage claims, they might require photos showing both packaging and contents, request carrier inspection for high-value items, or verify damage patterns match shipping damage vs. other causes.

                Legitimate issues will always be resolved fairly. These verification steps protect both customers and the business from fraud.

                Escalation:
                If you're not satisfied with the resolution proposed by the customer service representative, you can request escalation to a supervisor or manager. They can review the case and determine if additional action is appropriate.

                Be patient and respectful with customer service representatives - they want to help resolve your issue fairly and efficiently.

                Best Practices for Reporting Issues:
                Report issues as soon as you discover them - prompt reporting enables faster resolution. Be honest and accurate in your description - exaggerating or providing false information delays resolution. Provide all relevant information up front to avoid back-and-forth requests. Attach clear photos showing the problem when applicable. Respond quickly to requests for additional information. Keep your confirmation email and exception reference number for follow-up.

                The delivery exception system exists to ensure you receive what you ordered in perfect condition. Don't hesitate to use it when legitimate problems occur. Customer service is there to help make things right when deliveries don't go as planned.
                """,
                null
        );
    }

    private UpsertDocumentRequest createExceptionTypesDoc() {
        return new UpsertDocumentRequest(
                "Types of Delivery Exceptions in SLMS",
                Set.of("CUSTOMER", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("customer-portal", "admin-console"),
                Set.of(),
                "en-US",
                """
                Delivery exceptions in SLMS are categorized by type to help route issues to the appropriate resolution workflow and track patterns in delivery problems. Understanding exception types helps customers report issues accurately and helps customer service representatives handle them effectively.

                Overview of Exception Types:
                SLMS recognizes several standardized exception types based on common delivery problems. Each type represents a specific category of issue with typical resolution approaches. Exception types include damaged package, incorrect items, non-receipt despite delivery confirmation, wrong delivery location, late delivery beyond estimated date, customer delivery change request, and general or other issues.

                Let's explore each exception type in detail.

                Damaged Package Exceptions:
                This exception type covers packages that arrive with damage to the packaging, contents, or both. Damage can occur during various stages including warehouse handling, carrier pickup and loading, transit between facilities, or final delivery.

                Typical damaged package exceptions include crushed or dented boxes with items inside potentially damaged, broken items with intact external packaging suggesting internal shifting during transit, torn or punctured packaging exposing contents, water damage from exposure to moisture, or obvious signs of rough handling like tire marks or severe crushing.

                When reporting damaged package exceptions, customers should document the damage with photos showing all sides of the package, visible damage to contents, and any carrier markings or labels that might be relevant.

                Customer service resolution for damaged packages typically involves determining if items are usable despite packaging damage, arranging replacement shipment for damaged items, coordinating carrier insurance claims for high-value damage, or issuing refunds when replacement isn't possible or desired.

                Carriers are often liable for shipping damage, so detailed documentation helps with insurance claims and carrier accountability.

                Incorrect Items Exceptions:
                This exception type covers situations where the received package contains items that don't match the order. This can happen due to warehouse picking errors, packing mistakes, or label mix-ups.

                Common incorrect item exceptions include completely wrong items shipped, correct items but wrong quantities (too few or too many), missing items that were supposed to be included, or wrong variants like wrong size, color, or model of the ordered item.

                Customer service resolution typically involves verifying what was supposed to be shipped vs. what was received, arranging return of incorrect items, shipping correct items as replacement, and investigating the warehouse process to prevent similar errors.

                This exception type requires clear documentation of what was ordered, what was actually received, and photographic evidence if helpful.

                Non-Receipt Exceptions:
                Non-receipt exceptions occur when tracking shows the order was delivered but the customer didn't receive the package. This is a sensitive exception type because it could indicate several scenarios including misdelivery to wrong address, theft after delivery, package left in an unusual location the customer hasn't checked, driver scanning error showing delivered when it wasn't, or customer error like someone else in household received it.

                Customer service investigates non-receipt exceptions by reviewing driver delivery confirmation via QR code scan to verify delivery timestamp, checking delivery location details and any delivery photos, contacting the carrier for additional delivery information, verifying the shipping address was correct, and checking with neighbors or building management if applicable.

                Resolution depends on investigation findings. If the package was misdelivered or stolen, replacement or refund is typically provided. If it was delivered correctly but the customer overlooked it (like in a secure mail room), the customer is directed to the location. If driver error is confirmed, the carrier may still have the package for redelivery.

                Non-receipt exceptions require careful investigation to distinguish between legitimate non-receipt and customer error or fraud attempts.

                Wrong Delivery Location Exceptions:
                This exception type covers packages delivered to incorrect addresses or inappropriate locations. This might be entirely wrong address (like neighbor's house or different building), correct address but insecure location against delivery instructions, correct building but wrong unit or apartment, or package left outside despite instructions for secure delivery.

                Wrong delivery location exceptions are different from non-receipt because the customer knows where the package was incorrectly delivered or can see it was left in a wrong location.

                Customer service resolution involves determining why the delivery went to the wrong location (address error, driver error, instruction miscommunication), retrieving the package from the wrong location if possible, redelivering to the correct location, or replacing the package if it can't be retrieved.

                This exception type highlights the importance of clear addresses and delivery instructions.

                Late Delivery Exceptions:
                Late delivery exceptions occur when packages arrive significantly later than estimated delivery dates without explanation. While carriers don't guarantee delivery dates (only estimated dates), substantial delays may constitute exceptions especially for time-sensitive shipments.

                Common late delivery scenarios include packages sitting in one status for days without movement, packages missing carrier pickups or transit connections, weather or capacity delays that significantly extend delivery time, or customs delays for international shipments.

                Customer service response to late delivery exceptions depends on circumstances. If the package is still in transit, they may expedite remaining delivery or provide updated delivery estimates. If delay is severe, they might offer compensation, refund shipping charges, or arrange alternative shipping for replacements. For time-sensitive deliveries that arrive too late to be useful, refunds or cancellations may be appropriate.

                Late delivery exceptions help identify carrier performance issues and systemic logistics problems.

                Customer Delivery Change Request Exceptions:
                Sometimes customers need to change delivery details after the order is already in transit. This creates an exception for customer service to handle. Common change requests include update delivery address to new location, change delivery date or time, add special delivery instructions, hold package for customer pickup instead of delivery, or require signature confirmation for delivery.

                Customer service coordinates with carriers to implement these changes when possible. Address changes are easiest before the package reaches the destination facility. Date changes depend on carrier flexibility. Some changes may not be possible once delivery is imminent.

                This exception type is unique because it's customer-initiated rather than reporting a problem.

                Other or General Exceptions:
                This category covers delivery issues that don't fit standard types. Examples might include suspicious or tampered packaging suggesting security issues, hazardous materials handling concerns, customs or regulatory issues for international shipments, or special situations unique to specific orders.

                Customer service handles these case-by-case based on the specific circumstances.

                Exception Type Statistics and Trends:
                SLMS tracks exception frequency by type to identify patterns. If damaged package exceptions increase for a particular carrier, it might indicate handling problems. If incorrect item exceptions spike, it suggests warehouse quality control issues. These trends help logistics managers target improvements where they're most needed.

                How Exception Type Affects Resolution:
                Exception type determines which resolution workflows are available, how urgently the issue needs to be addressed, whether carrier coordination is required, whether warehouse process review is needed, and what documentation is necessary.

                Customer service representatives are trained on appropriate resolution approaches for each exception type to ensure consistent, fair handling.

                Multiple Exception Types on One Order:
                Sometimes a single order has multiple issues. For example, a package might be both late and damaged. In these cases, customers can report multiple exception types in one report. Customer service will address all issues in the resolution process.

                Choosing the Right Exception Type:
                When reporting an exception, selecting the correct type helps route your issue appropriately. Choose the type that best describes the primary problem. If you're unsure between two types, choose the one that most concerns you or describe both issues in the detailed description. The customer service representative can recategorize if needed.

                Understanding exception types helps ensure your delivery issues are properly classified, routed to the right resolution workflow, and resolved efficiently. Whether you're a customer reporting a problem or a customer service representative handling exceptions, clear exception types provide structure to the resolution process.
                """,
                null
        );
    }

    private UpsertDocumentRequest createDriverConfirmationDoc() {
        return new UpsertDocumentRequest(
                "How Driver Delivery Confirmation Works",
                Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("customer-portal", "driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                Driver delivery confirmation is a critical feature in SLMS that creates reliable, timestamped proof of delivery using QR code scanning. This system ensures accurate delivery records, prevents delivery disputes, and provides accountability throughout the delivery process. Understanding how driver confirmation works helps all stakeholders appreciate its importance.

                What is Driver Delivery Confirmation:
                Driver delivery confirmation is the process by which delivery drivers verify they have successfully delivered a package to its destination. In SLMS, this happens through a QR code scanning system. Each package has a unique QR code printed on its shipping label. When the driver delivers the package, they scan the QR code using the SLMS driver PWA (Progressive Web App) on their mobile device.

                This scan creates an instant, verifiable delivery confirmation that updates order status to DELIVERED, records the exact delivery timestamp, triggers customer notifications, and provides proof of delivery.

                Why QR Code Confirmation Matters:
                Traditional delivery confirmation methods had limitations. Drivers manually entering tracking numbers was slow and error-prone. Manual delivery logs could be lost or falsified. Customer signature capture required special equipment and didn't work for unattended deliveries. Carrier GPS tracking showed proximity but not actual delivery.

                QR code confirmation solves these problems by ensuring the exact package was confirmed (each QR code is unique to that order), creating instant electronic records that can't be lost, providing precise delivery timestamps automatically, working for both attended and unattended deliveries, and requiring minimal time from drivers (scanning takes seconds).

                This system creates accountability and trustworthy delivery records.

                How the QR Code System Works:
                When warehouse staff create a shipping label, SLMS generates a unique QR code for that order. This QR code encodes the tracking number and order details. The QR code is printed on the shipping label that gets attached to the package.

                When the driver arrives at the delivery location and successfully delivers the package, they open the SLMS driver PWA on their mobile device. The app has a "Scan QR Code" or "Confirm Delivery" button. Clicking this activates the device's camera. The driver points the camera at the QR code on the package label. The app automatically detects and scans the code.

                Once scanned, the app immediately sends the confirmation to the SLMS backend server. The server updates the order status to DELIVERED, records the precise timestamp of the scan, logs which driver account performed the scan, triggers automatic customer notification, and updates all dashboards and tracking interfaces.

                All of this happens within seconds, providing real-time delivery confirmation.

                For Drivers: Using the QR Code Scanner:
                Drivers use the confirmation system as part of their regular delivery routine. When you arrive at a delivery location, complete the delivery according to the customer's instructions (hand to recipient, leave at door, place in mailbox, etc.). Open the driver PWA on your mobile device. Tap the scan button to activate the camera. Point the camera at the QR code on the package label. Hold steady for a moment while the app focuses and scans.

                You'll see a confirmation message indicating successful scan and delivery recorded. The order immediately shows as delivered in the system. Move on to your next delivery.

                Best practices for drivers include scanning immediately after delivery rather than batching later, ensuring good lighting for the QR code scan, keeping the camera lens clean for reliable scanning, and reporting any damaged or unreadable QR codes to dispatch.

                For Customers: What Confirmation Means:
                When you receive a delivery confirmation notification, it means the driver physically scanned the QR code on your package. This creates high confidence the package was actually delivered rather than just a generic scan at a facility.

                The confirmation timestamp shows exactly when delivery occurred. If you're home when the timestamp shows delivery but didn't receive the package, this helps pinpoint the timeframe for investigation. If you're away, it tells you when to expect the package to be at your delivery location.

                Delivery confirmation triggers automatic notification via your preferred channel (email or SMS). You can also see the delivery timestamp in your order tracking timeline.

                This confirmation creates proof of delivery that's valuable if disputes or questions arise later.

                Delivery Confirmation and Non-Receipt Issues:
                The QR code confirmation system is particularly important when non-receipt exceptions occur. If a customer reports they didn't receive a package but tracking shows delivered, customer service can review the delivery confirmation details including exact timestamp, which driver account performed the scan, and location data if available.

                This information helps investigate whether the package was misdelivered to a neighbor, delivered to an unusual location on the property the customer hasn't checked, stolen after delivery, or incorrectly scanned before actual delivery.

                The reliability of QR code confirmation makes fraudulent non-receipt claims very difficult. It's nearly impossible for a package to show delivered via QR scan if the driver wasn't physically present with that specific package.

                What If the QR Code Can't Be Scanned:
                Occasionally QR codes might be unreadable due to printing errors, damage to the label during shipping, poor lighting conditions, or camera issues. When this happens, drivers should contact their dispatcher or warehouse for guidance. Alternative confirmation methods might be available, such as manual tracking number entry with verification, photo evidence of delivery plus manual entry, or carrier system confirmation.

                These alternative methods are exceptions to the standard QR process. They require additional verification to maintain delivery confirmation reliability.

                Offline Scanning and Delayed Upload:
                The driver PWA is designed to work in areas with poor cellular coverage. If a driver scans a QR code but doesn't have internet connectivity at that moment, the app queues the confirmation locally on the device. As soon as connectivity is restored, the app automatically uploads the pending confirmation to the server.

                This ensures deliveries in remote or low-coverage areas are still confirmed reliably without requiring drivers to remember to manually sync later.

                Security and Integrity:
                The QR code confirmation system includes several security features. Each QR code is unique and tied to a specific order - the same code can't be reused for different deliveries. Scans are tied to the driver's authenticated account - there's an audit trail of who performed each confirmation. Timestamps are generated by the server, not the device - they can't be falsified. Location data may be captured during scan for additional verification.

                These security features make the confirmation system trustworthy and resistant to manipulation.

                Confirmation for Different Delivery Scenarios:
                The QR code system works for various delivery situations. For deliveries handed directly to recipients, scan after handing over the package. For unattended deliveries left at doors or in designated locations, scan after placing the package in the agreed location. For deliveries to mailrooms or building offices, scan after leaving the package with the facility. For deliveries requiring signatures, obtain signature then scan QR code.

                The key is that scanning happens after the package is delivered and in its final location.

                Benefits for Warehouse Operations:
                Warehouse staff benefit from reliable delivery confirmation because it closes the order lifecycle with certainty, reduces disputes about whether delivery occurred, provides clear proof for billing and invoicing, and creates accountability for the entire fulfillment chain.

                When an order shows DELIVERED status with QR confirmation timestamp, the warehouse knows the job is complete.

                Benefits for Customer Service:
                Customer service representatives rely on delivery confirmation for exception resolution. When investigating non-receipt claims, the confirmation timestamp and details provide concrete evidence. When customers have questions about delivery timing, the precise timestamp gives accurate information. When resolving delivery disputes, QR confirmation is reliable proof.

                This reduces he-said-she-said disputes and provides factual basis for decisions.

                Delivery Confirmation Analytics:
                SLMS can analyze delivery confirmation data for operational insights. Metrics might include percentage of deliveries confirmed via QR code vs. alternative methods, average time between carrier pickup and delivery confirmation, which drivers have fastest or most reliable confirmation patterns, and time-of-day patterns for deliveries.

                These analytics help optimize delivery operations and driver performance.

                Future Enhancements:
                The QR code confirmation system can be extended with additional features like photo capture at delivery showing package placement, customer notification with delivery location photo, signature capture for high-value items, or enhanced location verification for precise delivery proof.

                These enhancements further improve delivery transparency and accountability.

                The driver delivery confirmation system through QR code scanning is a cornerstone of SLMS reliability. It provides instant, verifiable proof of delivery that benefits customers, drivers, warehouse operations, and customer service. Understanding how it works helps everyone appreciate its importance in ensuring successful, accountable deliveries.
                """,
                null
        );
    }

    private UpsertDocumentRequest createManagingExceptionsDoc() {
        return new UpsertDocumentRequest(
                "Managing Delivery Exceptions - Customer Service Guide",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Managing delivery exceptions is a core responsibility of customer service representatives in SLMS. Effective exception management resolves customer issues quickly, maintains satisfaction despite problems, and provides valuable feedback to improve logistics operations. This guide covers the complete exception management workflow for customer service teams.

                Exception Management Overview:
                As a customer service representative, you're responsible for guiding delivery exceptions from initial report through final resolution. This involves investigating what went wrong, determining appropriate solutions, coordinating with customers and carriers, implementing resolutions, and closing exceptions with complete documentation.

                Your role is critical because you're the bridge between frustrated customers and successful problem resolution. How you handle exceptions directly impacts customer satisfaction and retention.

                Viewing Exceptions Dashboard:
                When you log into the SLMS admin console, your customer service dashboard shows all delivery exceptions requiring attention. The dashboard provides an overview with key metrics including total open exceptions awaiting action, exceptions assigned to you personally, exception backlog and aging (how long exceptions have been open), high-priority exceptions needing immediate attention, and trends in exception types and volumes.

                This dashboard helps you prioritize your work and ensures no exceptions fall through the cracks.

                Exception Workflow Stages:
                Exceptions progress through defined workflow stages from report to resolution. Understanding these stages helps you manage exceptions systematically.

                OPEN status indicates a newly reported exception that hasn't been assigned yet. These exceptions appear in the unassigned queue for team leads or supervisors to allocate.

                ASSIGNED status means the exception has been assigned to a specific representative (often you) for investigation. Once assigned, you own that exception through resolution.

                IN_PROGRESS status shows you're actively working to resolve the exception. You've begun investigation, contacted parties, and are implementing solutions.

                RESOLVED status indicates the underlying issue has been addressed - replacement shipped, refund issued, package located, etc. The resolution is implemented but final closure documentation may be pending.

                CLOSED status is the final state. All resolution actions are complete, the customer is satisfied, and documentation is finalized.

                Effective exception management involves moving exceptions through these stages efficiently without skipping necessary investigation or documentation steps.

                Assigning Exceptions to Yourself:
                If you see unassigned exceptions in your queue that you can handle, you can assign them to yourself. Open the exception detail page, click the "Assign to Me" or similar button, and optionally add assignment notes indicating why you're taking this exception or your initial assessment.

                Once assigned, the exception moves to ASSIGNED status and appears in your personal work queue. You're now responsible for seeing it through to resolution.

                Investigating Exceptions:
                Thorough investigation is the foundation of good exception resolution. Start by reviewing all available information about the exception including the customer's reported issue description, exception type and severity, order details and shipping information, full tracking timeline, shipping documents and labels, and any photos or evidence the customer provided.

                Next, identify what additional information you need. For damaged packages, review photos to assess damage extent and determine if it's shipping damage vs. other causes. For non-receipt issues, check driver delivery confirmation details including exact timestamp and location. For incorrect items, compare what was ordered vs. what customer reports receiving. For late deliveries, identify where the delay occurred in the tracking timeline.

                You may need to contact external parties. For carrier-related issues, contact the carrier for tracking updates, delivery details, or damage claim information. For warehouse issues like incorrect items, coordinate with warehouse staff to verify what was shipped. For customer clarification, ask for additional photos, information, or verification of details.

                Document all investigation findings in the exception notes. This creates an audit trail and helps if the exception is escalated or needs to be reviewed later.

                Determining Appropriate Resolutions:
                Based on investigation findings, determine the best resolution approach. Common resolution options include arranging replacement shipment if items were damaged, incorrect, or lost; issuing refunds if replacement isn't possible or customer prefers refund; locating and redelivering packages that were misdelivered or held at facilities; rescheduling delivery with corrected information; initiating returns for incorrect items; providing compensation for significant inconvenience; or updating delivery instructions to prevent future issues.

                Consider several factors when choosing resolutions: customer preference and what resolution they're requesting, cost and operational impact of different options, speed to resolution (can we fix this quickly vs. lengthy process), fairness given the circumstances, and whether warehouse or carrier is at fault affecting who bears the cost.

                Always aim for win-win resolutions that satisfy the customer while being fair to the business.

                Communicating with Customers:
                Throughout exception handling, keep customers informed. Send an initial response when you begin investigating, acknowledging their issue and setting expectations. Provide updates as you make progress, explaining what you've found and what actions you're taking. Request additional information if needed to complete investigation. Send resolution notification explaining what was done and confirming the outcome. Follow up after resolution to ensure customer satisfaction.

                Communication should be prompt, clear, empathetic, and professional. Acknowledge customer frustration, explain what went wrong without excessive blame-shifting, clearly state what you're doing to resolve the issue, and set realistic timeframes.

                Customers appreciate transparency and proactive updates even when resolution takes time. Regular communication prevents frustration from uncertainty.

                Coordinating with Carriers:
                Many exceptions require carrier coordination. For lost or delayed packages, contact carriers to locate packages in their system or expedite delivery. For damage claims, file carrier insurance claims with required documentation. For address corrections, work with carriers to update delivery addresses or hold packages. For delivery failures, arrange redelivery attempts with special instructions.

                Different carriers have different processes and systems. Familiarize yourself with common carrier support channels and procedures for your major carriers.

                Document all carrier interactions in exception notes including who you spoke with, when, what they said, and any case or claim numbers provided.

                Coordinating with Warehouse:
                Some exceptions require warehouse coordination. For incorrect items, verify what was actually shipped and arrange correct item shipment. For missing documents, request warehouse upload required documents. For returns, coordinate return receipt and processing. For quality issues, provide feedback about recurring problems.

                Warehouse staff are your partners in resolution. Clear communication and mutual respect ensure smooth coordination.

                Implementing Resolutions:
                Once you've determined the appropriate resolution, implement it through the SLMS system. For replacements, create a new order in the system for the replacement items, potentially with expedited shipping, and update the exception with the new tracking number. For refunds, process refund through appropriate channels and update exception with refund details. For returns, generate return authorization and shipping label, send to customer with return instructions, and track return receipt. For redeliveries, coordinate with carrier and update customer with new delivery date.

                Ensure all resolution actions are documented in the exception record. This creates accountability and provides information if questions arise later.

                Updating Exception Status:
                As you work through the exception, update its status to reflect current state. When you begin active investigation, change from ASSIGNED to IN_PROGRESS. When you implement the resolution (ship replacement, issue refund, etc.), change to RESOLVED. When all actions are complete and customer is satisfied, change to CLOSED with final resolution notes.

                Keeping status current helps your team leads monitor workload and ensures exceptions don't linger in inappropriate states.

                Closing Exceptions:
                When closing an exception, add comprehensive resolution notes documenting what the issue was, what investigation revealed, what resolution was implemented, any coordination with carriers or warehouse, customer feedback or satisfaction confirmation, and any lessons learned or process improvement suggestions.

                Good closure documentation helps with future exception training, pattern analysis, and responding to any follow-up questions.

                Only close exceptions when truly complete. If you've arranged a replacement but it hasn't shipped yet, leave the exception in RESOLVED until the replacement is actually on its way. If customer response is pending, keep it active until they confirm satisfaction.

                Escalating Complex Exceptions:
                Sometimes exceptions are too complex, high-value, or politically sensitive for standard handling. Know when to escalate including exceptions involving high-dollar amounts above your authority, repeated issues with the same customer suggesting systemic problems, carrier disputes where you can't get resolution, customer threatening legal action or social media complaints, or situations where you're unsure of the appropriate resolution.

                Escalate to your supervisor or team lead with a summary of the issue, what you've investigated, why you're escalating, and your recommendation if you have one. Don't feel bad about escalating - that's what supervisors are for on complex cases.

                Exception Priority and Severity:
                Exceptions have priority and severity levels that affect how urgently they need attention. High priority might include time-sensitive shipments that are delayed, high-value items that are lost or damaged, customer service VIPs or accounts, or issues generating significant customer frustration.

                Critical severity might indicate complete delivery failure, significant financial loss, safety or security concerns, or legal/compliance issues.

                Use dashboard filtering to ensure high-priority and critical-severity exceptions get immediate attention before working on standard issues.

                Exception Patterns and Reporting:
                As you handle exceptions, watch for patterns. If you notice increasing exceptions of a particular type or involving specific carriers, warehouses, or destinations, report this to your supervisor. Pattern recognition helps identify systemic problems that need operational fixes beyond individual exception resolution.

                SLMS tracks exception metrics to support this analysis. Your observations combined with system data help drive continuous improvement.

                Best Practices for Exception Management:
                Respond to new exceptions within defined service levels (e.g., within 2 hours for high priority). Keep exception status current as you work. Document everything in exception notes - investigation, communication, actions taken. Set realistic customer expectations about resolution timeframes. Follow through on commitments - if you say you'll update them tomorrow, do it. Look for root causes, not just symptoms, to prevent recurring issues. Maintain professional, empathetic tone even with difficult customers. Collaborate with teammates, share knowledge about challenging cases. Use templates and resources for common scenarios to speed resolution.

                Effective exception management turns delivery problems into opportunities to demonstrate excellent customer service. Customers often remember how you handled their issue more than the fact that an issue occurred. By being responsive, thorough, and solution-oriented, you build customer loyalty even through challenging situations.
                """,
                null
        );
    }

    private UpsertDocumentRequest createExceptionStatusWorkflowDoc() {
        return new UpsertDocumentRequest(
                "Understanding Exception Status Workflow",
                Set.of("CUSTOMER", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("customer-portal", "admin-console"),
                Set.of(),
                "en-US",
                """
                Delivery exceptions in SLMS progress through a defined status workflow from initial report to final resolution. Understanding this workflow helps customers know what to expect and helps customer service representatives manage exceptions consistently. This guide explains each status stage and the transitions between them.

                Exception Status Overview:
                Every exception exists in one of five workflow statuses at any given time: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, and CLOSED. These statuses represent the exception's current state in the resolution process. As customer service works on the exception, its status changes to reflect progress toward resolution.

                For customers, exception status provides visibility into whether your issue is being worked on and how close it is to resolution. For customer service, status workflow provides structure and ensures consistent handling.

                OPEN Status - Initial Report:
                When a customer reports a delivery issue, the exception is created with OPEN status. OPEN means the exception has been received and is in the queue but hasn't been assigned to a specific customer service representative yet.

                Exceptions typically stay in OPEN status briefly - from minutes to a few hours depending on current service team workload. Team leads or supervisors review OPEN exceptions and assign them to available representatives based on workload, expertise, or assignment rules.

                For customers, OPEN status means your issue has been received and is queued for assignment. You should receive an initial confirmation that your exception was reported.

                Transition from OPEN to ASSIGNED:
                The transition from OPEN to ASSIGNED happens when a customer service representative or supervisor assigns the exception to a specific representative. This might be automatic based on assignment rules, manual assignment by a team lead, or self-assignment by representatives taking exceptions from the queue.

                Once assigned, the exception moves to ASSIGNED status and the assigned representative becomes responsible for investigation and resolution.

                ASSIGNED Status - Exception Ownership:
                ASSIGNED status means the exception has been assigned to a specific customer service representative who will handle it through resolution. The representative's name appears on the exception record, creating clear ownership and accountability.

                Exceptions may stay in ASSIGNED status from hours to a day or more depending on the representative's current workload and the exception's priority. High-priority exceptions should move to IN_PROGRESS status quickly, while lower-priority exceptions might wait if the representative is handling urgent cases.

                For customers, ASSIGNED status means a specific person is now responsible for your issue and will begin investigating soon. You may receive notification of assignment depending on system configuration.

                For customer service representatives, ASSIGNED status exceptions appear in your work queue awaiting your attention. You should begin investigation and move them to IN_PROGRESS as soon as you actively start working on them.

                Transition from ASSIGNED to IN_PROGRESS:
                The transition from ASSIGNED to IN_PROGRESS happens when the representative begins active investigation and resolution work. This might be triggered by opening the exception and beginning review, sending initial customer communication acknowledging the issue, contacting carriers or warehouse for information, or taking other investigation actions.

                This transition shows the exception is being actively worked rather than just sitting in a queue.

                IN_PROGRESS Status - Active Resolution Work:
                IN_PROGRESS is the active work status where customer service representatives investigate issues and implement solutions. This is typically the longest status period as representatives review details, gather information, coordinate with parties, determine appropriate resolutions, and implement resolution actions.

                IN_PROGRESS duration varies widely based on exception complexity. Simple issues like verifying an address error might be IN_PROGRESS for just hours. Complex cases requiring carrier investigation, high-value damage claims, or coordination across multiple parties might stay IN_PROGRESS for several days.

                For customers, IN_PROGRESS status means your issue is being actively investigated and resolved. You should receive updates from the representative about their progress and what they're doing. If your exception sits in IN_PROGRESS for longer than expected without updates, you can contact customer service for a status check.

                For customer service representatives, IN_PROGRESS is where you do your core work. Keep the customer updated, document your investigation in exception notes, coordinate with necessary parties, and work toward resolution.

                While IN_PROGRESS, representatives can add notes, send customer messages, update priority or severity if needed, and attach documents. All of this activity is logged in the exception history.

                Transition from IN_PROGRESS to RESOLVED:
                The transition to RESOLVED happens when the representative implements the resolution. This means the underlying issue has been addressed through actions like replacement shipment created and dispatched, refund processed, package located and redelivery arranged, corrected information updated with carrier, or return initiated and label sent.

                RESOLVED doesn't mean the customer has received the solution yet - it means the solution has been implemented and is in motion. For example, if the resolution is a replacement shipment, RESOLVED status occurs when that replacement is shipped, not when it's delivered.

                RESOLVED Status - Solution Implemented:
                RESOLVED status indicates the exception's resolution has been implemented and is in effect. The problem is solved, though some actions like replacement delivery may still be in progress.

                For customers, RESOLVED status means the solution to your issue is now in effect. If it was a replacement, the replacement is on its way. If it was a refund, the refund has been processed. You should receive notification of the resolution with details about what was done.

                For customer service representatives, RESOLVED status means your active work is complete. You've implemented the solution and communicated it to the customer. You may keep the exception in RESOLVED status while monitoring to ensure the solution works - for example, waiting to confirm the replacement is actually delivered.

                Exceptions typically don't stay in RESOLVED status very long - from a few hours to a few days while confirming the resolution is successful.

                Transition from RESOLVED to CLOSED:
                The final transition is from RESOLVED to CLOSED. This happens when the resolution is confirmed successful and complete, the customer confirms satisfaction or acknowledges the resolution, all documentation and notes are finalized, and no further action is needed.

                Customer service representatives should close exceptions once they're truly complete. Don't leave exceptions lingering in RESOLVED if they're actually done.

                CLOSED Status - Final Completion:
                CLOSED is the terminal status indicating the exception is fully resolved and requires no further action. The issue was reported, investigated, resolved, and completed. The customer's problem is fixed.

                CLOSED exceptions remain in the system as historical records for reference, analysis, and documentation but no longer appear in active work queues.

                For customers, CLOSED status means your issue is completely resolved. If problems recur, you would report a new exception rather than reopening the closed one.

                For customer service representatives, CLOSED exceptions represent completed work. The closure notes should comprehensively document what happened and how it was resolved for future reference.

                Administrators and managers analyze CLOSED exceptions to identify trends, measure performance, and improve processes.

                Status Changes Shouldn't Go Backwards:
                Exceptions generally flow forward through the status sequence: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED. Statuses shouldn't normally move backwards. If you discover a supposedly resolved exception actually isn't resolved, rather than moving status backward, document the new development in notes and continue working forward.

                The only exception might be re-opening a recently closed exception if it becomes clear the issue wasn't actually resolved.

                Skipping Status Stages:
                While the standard workflow includes all five statuses, some exceptions might skip stages in specific circumstances. For example, a very simple exception might go directly from ASSIGNED to RESOLVED if investigation and resolution are nearly instantaneous. An exception that gets closed due to customer withdrawal might go from IN_PROGRESS directly to CLOSED.

                These shortcuts are acceptable when appropriate, but generally following the full workflow provides better documentation and tracking.

                How Long Should Each Status Last:
                While duration varies by exception complexity, general guidelines help set expectations. OPEN status should last minutes to hours - just long enough for assignment. ASSIGNED status should last hours to at most a day before work begins. IN_PROGRESS is the variable duration status - hours for simple issues, days for complex ones. RESOLVED status should last just until final confirmation, typically hours to a couple days. Transition to CLOSED should happen promptly once everything is complete.

                Exceptions that sit in one status for much longer than expected may indicate problems like workload issues preventing representatives from starting work, difficulty getting needed information or coordination, complex issues requiring extended investigation, or exceptions that were forgotten or fell through cracks.

                Exception aging reports help identify these stalled exceptions for attention.

                Viewing Status in Customer Portal:
                Customers can see their exception status in the customer portal. On the order detail page, exceptions appear with their current status and a brief description. Clicking the exception shows detailed history including all status changes with timestamps, notes and updates from customer service, communication messages, and resolution details.

                This transparency helps customers understand progress without needing to call and ask for updates.

                Status Notifications:
                Depending on configuration, customers may receive automatic notifications when exception status changes. Common notification triggers include when exception is ASSIGNED showing who is handling your issue, when status changes to RESOLVED with resolution details, and when status is CLOSED confirming completion.

                Representatives may also send manual updates during IN_PROGRESS to keep customers informed.

                Exception Status Reports and Analytics:
                SLMS tracks exception status data for reporting and analysis. Metrics include how many exceptions are in each status at any time, average time spent in each status, aging of exceptions by status, and representative workload by exception count and status.

                These reports help managers balance workload, identify bottlenecks, and ensure exceptions are resolved within service level commitments.

                Understanding the exception status workflow provides structure to the resolution process. Whether you're a customer tracking your issue or a customer service representative managing exceptions, the status stages create clear expectations and accountability for resolving delivery problems efficiently.
                """,
                null
        );
    }

    private UpsertDocumentRequest createAssigningExceptionsDoc() {
        return new UpsertDocumentRequest(
                "Exception Assignment and Team Management",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Exception assignment is the process of allocating delivery exceptions to specific customer service representatives for investigation and resolution. Effective assignment ensures exceptions are handled promptly, workload is balanced across the team, and representatives with appropriate expertise handle complex cases. This guide covers assignment strategies, workload management, and team coordination for exception handling.

                Why Exception Assignment Matters:
                Proper exception assignment is critical for several reasons. Clear assignment creates accountability - each exception has an owner responsible for resolution. Assignment enables workload balancing so no representative is overwhelmed while others are idle. It allows matching exceptions to representative expertise for efficient resolution. Assignment creates visibility for managers to monitor team performance and capacity. It prevents exceptions from being overlooked or duplicated if multiple people try to handle the same issue.

                Without clear assignment, exceptions can languish in the queue with everyone assuming someone else will handle them, or multiple people might work on the same issue causing confusion and wasted effort.

                Who Can Assign Exceptions:
                Depending on your organization's structure and SLMS configuration, different roles may assign exceptions. Team leads or supervisors often assign exceptions to their team members based on workload and expertise. Administrators with full system access can assign any exception. Customer service representatives may be able to self-assign exceptions from the unassigned queue. Automatic assignment rules may assign exceptions based on criteria like exception type, priority, or round-robin distribution.

                Your specific permissions determine whether you can assign exceptions to others or only to yourself.

                Viewing Unassigned Exceptions:
                Unassigned exceptions appear in the OPEN status queue. Access this queue through the exception management interface with filters set to show OPEN exceptions. These are exceptions that have been reported but not yet assigned to anyone.

                The unassigned queue typically displays exceptions with key information including exception type and description, associated order and tracking number, customer information, priority and severity, and how long the exception has been waiting (age).

                Priority and high-severity exceptions should be assigned first to ensure urgent issues get immediate attention.

                Assigning Exceptions to Team Members:
                If you're a team lead or supervisor assigning exceptions to your team, follow this process. Review unassigned exceptions in the queue, considering their type, complexity, and priority. Check your team members' current workload to identify who has capacity. Match exception characteristics to representative expertise - for example, assign complex carrier dispute cases to experienced reps. Open the exception detail and select the representative from the assignment dropdown. Optionally add assignment notes explaining why this rep is appropriate or highlighting important details. Save the assignment.

                The exception moves to ASSIGNED status and appears in that representative's work queue. They may receive notification of the new assignment.

                Self-Assigning Exceptions:
                If your organization allows representatives to self-assign from the queue, you can take ownership of exceptions you're ready to work on. Browse the unassigned exception queue, looking for exceptions matching your skills or availability. Select an exception you can handle. Click "Assign to Me" or similar function. Optionally add notes about why you're taking this exception. The exception now appears in your assigned queue as your responsibility.

                Self-assignment works well for experienced teams where representatives know their capacity and can choose appropriate work. It requires discipline to ensure high-priority exceptions get taken quickly and that representatives don't cherry-pick only easy cases while avoiding difficult ones.

                Automatic Assignment Rules:
                Some SLMS configurations support automatic assignment based on rules. Common automatic assignment approaches include round-robin distribution that assigns exceptions to representatives in rotation to balance workload, priority-based assignment that routes high-priority exceptions to specific reps or teams, exception type assignment that routes particular exception types to representatives with relevant expertise, territory assignment that assigns exceptions based on geographic region or customer account, and workload-based assignment that gives new exceptions to whoever currently has the fewest active exceptions.

                Automatic assignment reduces manual allocation work but requires good rules configuration to be effective. Managers should monitor automatic assignment to ensure it's working as intended.

                Reassigning Exceptions:
                Sometimes exceptions need reassignment after initial assignment. Common reassignment scenarios include representative is out sick or on vacation and can't handle assigned exceptions, exception turns out to be more complex than initially assessed and requires different expertise, workload rebalancing when one representative is overloaded, customer request to work with a different representative, or escalation to senior representatives or supervisors.

                To reassign, open the exception, change the assigned representative to the new person, add notes explaining the reassignment reason, and notify the previous and new assignees if needed. The exception remains in its current status but ownership transfers.

                Balancing Workload:
                Team leads should monitor workload distribution to ensure balance. Check how many active exceptions each representative has. Look at exception complexity - ten simple exceptions might be easier than three complex ones. Consider representative experience and skill level when allocating difficult cases. Monitor exception aging to identify representatives who may be struggling to keep up. Redistribute work if significant imbalances appear.

                Balanced workload improves team morale, prevents burnout, and ensures consistent service levels across all exceptions.

                Matching Expertise to Exceptions:
                Not all representatives have equal skills in all exception types. Effective assignment considers expertise. For example, assign complex carrier dispute cases to representatives with strong carrier relationships and negotiation skills. Route international shipping exceptions requiring customs knowledge to appropriate reps. Give damaged high-value item exceptions to experienced reps who can handle insurance claims. Let newer representatives handle straightforward cases to build experience while veterans tackle complex situations.

                Over time, identify each representative's strengths and route exceptions accordingly.

                Priority and Escalation Assignment:
                High-priority and escalated exceptions may require special assignment approaches. Critical exceptions might be immediately assigned to senior representatives or team leads. VIP customer exceptions might route to designated account managers. Exceptions with potential legal or PR implications might go straight to supervisors. Multi-exception cases affecting one customer might all be assigned to the same rep for consistency.

                Have clear escalation paths so representatives know when and how to escalate exceptions beyond their authority or expertise.

                Viewing Your Assigned Exceptions:
                As a representative, your assigned exceptions appear in your personal work queue. Filter exceptions to show those assigned to you with status ASSIGNED or IN_PROGRESS. This is your active workload - exceptions you're responsible for resolving.

                Sort your queue by priority to ensure urgent exceptions get attention first, by aging to identify exceptions that have been waiting longest, by exception type to batch similar work, or by customer to see if one customer has multiple issues.

                Effective queue management prevents exceptions from being overlooked.

                Managing Your Workload:
                Monitor your active exception count and complexity. If you're assigned more exceptions than you can reasonably handle, communicate with your team lead about workload concerns. If you're assigned a complex exception that will take significant time, let your lead know so they can adjust other assignments. If you become ill or need time off, communicate about reassigning your active exceptions.

                Proactive workload communication prevents exceptions from stalling.

                Exception Assignment Analytics:
                Managers can analyze assignment data to optimize team performance. Metrics include exceptions per representative showing workload distribution, average time from assignment to resolution by representative measuring efficiency, reassignment frequency indicating whether initial assignments are appropriate, exception aging by assignee identifying who might need support, and representative performance by exception type showing strengths and training needs.

                These analytics help optimize assignment strategies and identify coaching opportunities.

                Assignment Notifications:
                Representatives should be notified when exceptions are assigned to them. Notification methods might include email alerts when new exceptions are assigned, dashboard updates showing new assignments in your queue, mobile notifications for high-priority assignments, or periodic summary emails of your current workload.

                Reliable notifications ensure representatives don't miss new assignments.

                Bulk Assignment:
                For high-volume operations, managers may need bulk assignment capabilities to assign multiple exceptions at once based on filters. This speeds up allocation but requires care to ensure appropriate assignments.

                Team Collaboration on Exceptions:
                While exceptions are formally assigned to one owner, team collaboration can help resolution. Representatives can consult colleagues about challenging cases, ask for help with specific tasks like carrier contact, or hand off temporarily if specific expertise is needed. Document these collaborations in exception notes for future reference.

                Exception Assignment Best Practices:
                Assign exceptions quickly - unassigned exceptions should be minimal. Balance workload fairly across team members. Match expertise to exception complexity. Communicate with your team about capacity and challenges. Use priority and severity to guide assignment decisions. Monitor aging to ensure no exceptions stall. Document reassignment reasons for accountability. Review assignment patterns to identify improvement opportunities.

                Effective exception assignment is the foundation of efficient exception management. Clear ownership, balanced workload, and appropriate expertise matching ensure delivery issues are resolved quickly and consistently, maintaining customer satisfaction even when problems occur.
                """,
                null
        );
    }

    private UpsertDocumentRequest createContactingPartiesDoc() {
        return new UpsertDocumentRequest(
                "Contacting Parties During Exception Resolution - Communication Guide",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Resolving delivery exceptions often requires contacting and coordinating with various parties including customers, carriers, warehouse staff, and drivers. Effective communication with these stakeholders is essential for gathering information, implementing solutions, and ensuring successful resolution. This guide covers best practices for contacting each party type during exception handling.

                Communication Overview:
                As a customer service representative managing exceptions, you serve as the central point of contact between all parties involved in resolution. You gather information from customers, coordinate with carriers and warehouse staff, communicate updates back to customers, and ensure everyone has the information they need to resolve the issue.

                Clear, timely, professional communication is critical to exception resolution success.

                Communicating with Customers:
                Customer communication should be prompt, empathetic, and transparent. Initial contact should acknowledge their issue within established service levels (e.g., within 2 hours for high-priority exceptions). Send an acknowledgment message that thanks them for reporting the issue, confirms you've received their exception, introduces yourself as their representative, and sets expectations for resolution timeframe.

                During investigation, provide regular updates. If investigation takes longer than a day, send progress updates even if you don't have a resolution yet. Updates might include what you've discovered so far, who you're coordinating with, what additional information you need, and when they can expect the next update.

                When requesting additional information from customers, be specific. Instead of "send more photos," say "please send photos showing all four sides of the package and any visible damage to the contents inside." Explain why you need the information and how it will help resolve their issue.

                When communicating resolution, be clear and complete. Explain what went wrong, what resolution you're implementing, when they can expect the resolution (e.g., when replacement will arrive), what they need to do if anything (e.g., return the incorrect item), and how to contact you with questions.

                Use appropriate tone with customers. Be empathetic - acknowledge their frustration. Be apologetic when appropriate without over-apologizing or accepting blame for things beyond SLMS control. Be professional and helpful. Be positive and solution-oriented rather than defensive.

                Respond to customer messages promptly. If they ask a question, answer within the same day if possible. Prompt responses build trust and prevent escalation.

                Communicating with Carriers:
                Many exceptions require carrier contact to locate packages, update delivery information, file damage claims, arrange redeliveries, or resolve delivery failures. Each carrier has different contact methods - customer service phone numbers, email support, online portals, or account representatives for high-volume shippers.

                When contacting carriers, have necessary information ready including tracking number, order details, specific issue description, and what you need from them (locate package, file claim, schedule redelivery, etc.). Be prepared to provide proof of the issue such as photos of damage or delivery confirmation details.

                Document all carrier interactions thoroughly in exception notes including date and time of contact, carrier representative name and ID if available, what information you provided, what they told you, any case or claim numbers they generated, what actions they committed to, and when they said you'd hear back or see results.

                Common carrier contact scenarios include package location requests when tracking shows no movement, asking for package whereabouts and estimated delivery, filing damage claims, providing required photos and documentation, following carrier's claim process, and obtaining claim numbers and timeframes. Address corrections involve providing the correct address and requesting redelivery to updated location, confirming whether change is possible given package current location. Delivery failures need investigation of why delivery failed, arranging new delivery attempt with updated instructions, or requesting package hold for customer pickup.

                Be professional but persistent with carriers. Carrier customer service can be challenging - long wait times, difficulty getting answers, or representatives who can't help. Stay patient but follow up regularly. Escalate within the carrier if you're not getting needed support. Some carriers provide dedicated contacts for business accounts - use these relationships for better service.

                Remember that carriers are partners, not adversaries. Frame requests collaboratively. Instead of "your driver screwed up," say "we need to understand what happened with this delivery so we can resolve it for our customer."

                Communicating with Warehouse Staff:
                Warehouse coordination is needed for exceptions involving incorrect items, missing documents, quality issues, or return processing. Contact warehouse staff through established channels - direct email or phone to warehouse managers, internal messaging systems, or ticketing systems if your organization uses them.

                When contacting warehouse about incorrect items, provide the order number and tracking number, what was supposed to be shipped vs. what customer reports receiving, and what you need from them (verify what was actually picked and packed, prepare correct items for replacement shipment, investigate process to prevent recurrence).

                For missing or incorrect documents, specify the order and document type needed, explain why it's needed (customer request, claim requirement, etc.), and request upload to the order in SLMS.

                For return coordination, communicate the return authorization details, when return shipment is expected, what warehouse should do upon receipt (inspect, restock, discard), and request confirmation when return is processed.

                When reporting patterns, if you notice recurring exceptions from a warehouse (frequently picking wrong items, consistent packing damage, etc.), report these patterns to warehouse managers for process improvement. Provide specific examples and data to support your observations.

                Maintain professional collaborative relationships with warehouse staff. They're your partners in resolution. Frame communications cooperatively, acknowledge they're busy and managing volume, say please and thank you, and provide all necessary information up front to avoid back-and-forth.

                Communicating with Drivers:
                Direct driver communication is less common but occasionally needed. Scenarios might include confirming delivery details for non-receipt exceptions, getting driver's account of delivery attempt failures, providing updated delivery instructions for redelivery, or reporting issues with delivery handling.

                Drivers may be independent contractor delivery personnel, carrier employees, or your own company's delivery staff. Contact methods vary - often through their dispatcher or manager rather than directly.

                When driver information is needed, work through appropriate channels respecting their time and focusing on necessary facts.

                Internal Communication with Team:
                Communicate with your customer service team and management. Update team leads on exception status, especially for complex or escalated cases. Request help from colleagues when you need specific expertise or assistance. Share learnings from challenging exceptions that might help teammates. Escalate appropriately when exceptions exceed your authority or ability to resolve. Participate in team meetings discussing exception trends and improvements.

                Internal communication improves team effectiveness and ensures management visibility into issues.

                Communication Documentation:
                Document all communications in exception notes. Record when you contacted each party, what information was exchanged, what commitments were made, and what follow-up is needed. This documentation provides an audit trail, helps with hand-offs if exceptions are reassigned, supports escalations with complete history, and enables analysis of communication patterns and bottlenecks.

                Good documentation is as important as the communication itself.

                Communication Timing:
                Timing matters in exception communication. Acknowledge new exceptions quickly within service level commitments. Provide updates regularly - don't let customers wonder what's happening. Respond to inquiries the same day when possible. Follow up on commitments - if you said you'd update someone tomorrow, do it. Close the loop - when issues are resolved, confirm with all parties.

                Timely communication prevents frustration and builds trust.

                Communication Templates and Tools:
                Develop templates for common communications to save time while maintaining consistency. Templates might include initial exception acknowledgment, request for additional photos or information, resolution notification for common resolution types, and closure confirmation. Customize templates for each situation - don't send generic responses that don't address specific details.

                SLMS may provide built-in messaging tools for customer communication. Use these rather than external email when possible to keep all communication in the exception record.

                Multilingual Communication:
                If you serve customers in multiple languages, ensure appropriate language support. Use translation tools carefully - have native speakers verify important communications. Route exceptions requiring non-English communication to representatives with appropriate language skills. Recognize that communication nuances vary across cultures.

                Communication Escalation:
                Know when to escalate communication issues. If a customer is extremely upset and you can't de-escalate, involve a supervisor. If a carrier won't provide needed assistance, escalate within their organization. If warehouse repeatedly doesn't respond or help, escalate to warehouse management. If you're getting conflicting information from parties, escalate for manager intervention.

                Early escalation of communication impasses prevents exceptions from stalling.

                Communication Best Practices:
                Be prompt - respond quickly to messages and provide regular updates. Be clear - use simple language, specific details, and concrete next steps. Be empathetic - acknowledge frustration and concerns. Be honest - don't promise what you can't deliver or make excuses for failures. Be professional - maintain respectful tone with all parties. Be thorough - provide all necessary information to avoid back-and-forth. Be documented - record all communications in exception notes.

                Effective communication transforms exception management from a mechanical process into relationship-building opportunities. How you communicate often matters more than what you communicate. Clear, empathetic, timely communication with all parties ensures efficient exception resolution and maintains stakeholder satisfaction even when delivery problems occur.
                """,
                null
        );
    }

    private UpsertDocumentRequest createReschedulingReturnsDoc() {
        return new UpsertDocumentRequest(
                "Rescheduling Deliveries and Managing Returns",
                Set.of("CUSTOMER", "CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("customer-portal", "admin-console"),
                Set.of(),
                "en-US",
                """
                Rescheduling deliveries and managing returns are common resolution approaches for delivery exceptions. When packages can't be delivered as originally planned or when incorrect or damaged items need to be returned, these processes ensure customers receive their orders correctly. This guide covers how rescheduling and returns work in SLMS.

                When Rescheduling is Needed:
                Delivery rescheduling becomes necessary in several situations. Address corrections occur when the customer realizes they provided an incorrect address or moved since ordering. Failed delivery attempts happen when customers weren't available and need a new delivery time. Delivery preference changes arise when customers want to change from business delivery to home delivery or vice versa. Holds for pickup occur when customers want to pick up packages at carrier facilities instead of delivery. Time window requests happen when customers need delivery at specific times due to availability.

                Rescheduling allows packages already in the carrier network to reach customers correctly rather than being returned or lost.

                How Customers Request Rescheduling:
                Customers can request delivery rescheduling by reporting an exception through the customer portal, selecting "delivery change request" or similar exception type, describing what they need changed (address, date, delivery location), providing the corrected information, and explaining the reason for the change. The exception is assigned to customer service for coordination with carriers.

                Alternatively, customers might contact customer service directly via phone or email with rescheduling requests.

                How Customer Service Handles Rescheduling:
                When you receive a rescheduling request, assess whether the change is possible based on package current location in the carrier network, type of change requested, carrier capabilities and policies, and timing of the request. Changes are easiest before the package reaches the destination delivery facility.

                For address changes, verify the new address is complete and correct. Confirm whether the address change is within the same delivery zone or requires rerouting. Contact the carrier to request address update, providing tracking number and new address. Obtain confirmation from carrier that change was made and new delivery date if affected. Update customer with confirmation and any new delivery timeline. Document all changes in exception notes and order records.

                For delivery date changes, check if carrier offers scheduled delivery options. Request the customer's preferred new delivery date or time window. Coordinate with carrier to schedule delivery accordingly. Confirm with customer that new date is scheduled. Note that not all carriers support specific delivery scheduling, especially for standard service levels.

                For holds and pickup requests, request that carrier hold package at local facility for customer pickup. Provide customer with facility location and pickup instructions, including any ID or reference needed. Confirm package is being held and when it will be available. Document hold details for customer.

                Update the exception status to RESOLVED once rescheduling is confirmed and IN_PROGRESS while coordinating. Close the exception when the package is successfully delivered or picked up with the new arrangements.

                Address Change Limitations:
                Not all address changes are possible. Once a package is out for delivery, address changes typically can't be made. International address changes may be impossible once the package has left the origin country. Some carriers don't support address changes mid-transit. Address changes to different service zones may require additional shipping charges.

                Communicate limitations honestly with customers. If an address change isn't possible, explore alternatives like having package held for pickup, attempting delivery to original address if someone will be there, or returning to sender for re-shipment to correct address.

                When Returns are Necessary:
                Returns are needed when customers received incorrect items that need to be sent back, when damaged items are being replaced and damaged items need return, when customers decide they don't want items for various reasons, or when packages were delivered to wrong locations and need to come back. Returns enable warehouse to receive items back, verify their condition, restock usable items, and complete replacement or refund processes.

                How Customers Initiate Returns:
                For exception-related returns, customers report the exception through the customer portal describing the issue requiring return. Customer service determines return is appropriate resolution and initiates the return process. For non-exception returns like buyer's remorse, customers may use a separate returns request process depending on business policies.

                Customer Service Return Process:
                When a return is needed as exception resolution, create return authorization in SLMS or your return management system, generating a unique return authorization number. Generate return shipping label through carrier integration or manual label creation. The label should ship to appropriate warehouse and be prepaid if return is due to error rather than customer choice.

                Send return label and instructions to customer via email, including the label as PDF attachment, return authorization number, return shipping address, instructions for packaging the items, any required documentation to include, and timeline for return shipment. Update exception notes with return authorization details.

                Communicate return expectations with warehouse staff, informing them to expect the return, providing return authorization number and order reference, describing what items to expect, explaining what to do upon receipt (inspect, restock, discard, etc.), and requesting confirmation when return is received.

                Track return shipment using return tracking number. Monitor for return delivery to warehouse. Follow up if return isn't shipped within expected timeframe or doesn't arrive at warehouse. Process replacement or refund once return is confirmed received if that was part of the resolution.

                Return Shipping Costs:
                Who pays return shipping depends on the reason for return. If return is due to incorrect items shipped, damaged items, or other SLMS or carrier error, provide prepaid return label at no cost to customer. If return is customer error or preference, customer may need to pay return shipping or return shipping cost may be deducted from refund, depending on business policy.

                Be clear about return shipping cost responsibility when communicating with customers.

                Return Packaging Instructions:
                Instruct customers to package returns properly. Use original packaging if possible or comparable protective packaging. Include return authorization number and documentation inside package. Attach return label securely to outside. Remove or cover any old shipping labels. For damaged items, include all pieces and packaging materials for carrier claim evidence.

                Proper return packaging prevents damage during return shipment.

                Tracking Returns:
                Return labels should generate tracking numbers. Provide this tracking number to customers so they can confirm when carrier picks up the return. Use tracking to monitor return progress to warehouse. Track return delivery timestamp to know when warehouse should receive it. Follow up if tracking shows issues or delays.

                Return tracking ensures accountability and prevents lost returns.

                Processing Returns at Warehouse:
                Warehouse staff process returns upon receipt. They receive and scan the return package, verify contents match return authorization, inspect items for condition, update SLMS to confirm return receipt, process items according to instructions (restock, discard, hold for claim inspection), and notify customer service of receipt and condition.

                Customer service should coordinate with warehouse if expected returns don't arrive or if condition discrepancies need investigation.

                Completing Resolution After Return:
                Once return is received, complete the exception resolution. If replacement was part of the resolution, verify replacement was shipped or ship it now. If refund was promised, process refund now that return is confirmed. Update exception status to RESOLVED and then CLOSED once all actions are complete. Confirm with customer that resolution is complete.

                Returns Without Exception Basis:
                Some organizations allow general returns separate from delivery exceptions - for example, buyer's remorse returns or fit/preference issues. These may follow different processes and policies than exception-based returns.

                As customer service, understand your organization's general return policies and how they differ from exception-based returns.

                International Returns:
                Returns of international shipments add complexity. Customs documentation may be required for returns crossing borders. Return shipping costs can be high. Customers may be responsible for return duties and taxes. Processing times are longer.

                For international returns, provide clear instructions about customs forms, explain cost and timeline expectations, and coordinate with warehouse on international return receipt.

                Return Timeframes:
                Set expectations for return timeframes. Customers typically should ship returns within 7-14 days of receiving return authorization. Transit time back to warehouse varies by shipping method and distance. Warehouse processing upon receipt takes 1-3 business days. Final resolution (replacement ship or refund process) occurs after warehouse confirmation.

                Communicate these timeframes so customers know when to expect final resolution.

                What If Customer Doesn't Return Items:
                Sometimes customers request returns but don't actually ship items back. Follow up if return label is sent but tracking shows it wasn't used within expected timeframe. Contact customer to ask if they need help with return process or if they decided to keep items. After reasonable time (e.g., 30 days), close the return as incomplete if customer doesn't follow through.

                For high-value items, your organization may have policies about charging customers if items aren't returned as committed.

                Return Exceptions:
                Occasionally returns themselves have exceptions - return package lost in transit, return arrives damaged, customer returns wrong items. Treat these as new exceptions requiring investigation and resolution.

                Rescheduling and Returns Best Practices:
                For rescheduling, act quickly - address changes are easier the sooner you make them. Verify all information - double-check corrected addresses before submitting to carriers. Confirm changes - get carrier confirmation and communicate it to customers. For returns, provide clear instructions - remove ambiguity about how to return items. Track everything - monitor return shipments to prevent lost returns. Coordinate with warehouse - ensure they're ready to receive and process returns. Close the loop - confirm final resolution with customers after returns complete.

                Effective rescheduling and return management ensure delivery problems are resolved even when packages need to change course or come back. These processes require coordination across customers, carriers, and warehouse, with customer service orchestrating all the pieces to successful resolution.
                """,
                null
        );
    }

    // ================================
    // WAREHOUSE OPERATIONS DOCUMENTS
    // ================================

    private UpsertDocumentRequest createWarehouseOrderManagementDoc() {
        return new UpsertDocumentRequest(
                "Warehouse Order Management - Processing and Fulfillment",
                Set.of("WAREHOUSE_STAFF", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Warehouse order management in SLMS encompasses all activities from when an order is created through carrier pickup. Efficient warehouse operations ensure orders are fulfilled accurately, packaged securely, documented properly, and ready for delivery on time. This comprehensive guide covers the complete warehouse workflow.

                Warehouse Role in Order Lifecycle:
                Warehouse staff are responsible for the critical middle phase of every order's journey. You transform digital orders in the system into physical packages ready for delivery. Your work includes receiving and validating order details, picking correct items from inventory, packing items securely to prevent damage, generating and attaching shipping labels, uploading required documents, updating order statuses to reflect progress, staging packages for carrier pickup, and confirming carrier collection.

                Quality warehouse operations directly impact customer satisfaction, delivery success rates, and operational costs.

                Accessing Order Queues:
                Log into the SLMS admin console and navigate to the Orders section. Your order queue shows all orders that need warehouse processing. Use filters to organize your work by status to see orders at specific processing stages, by warehouse if you operate multiple facilities, by date to prioritize older orders, by shipping method to batch similar orders, or by destination to optimize picking routes.

                Focus on orders in CREATED status - these are newly entered and awaiting fulfillment.

                Order Information Review:
                Before processing an order, review all details to ensure you have everything needed. Check customer information including shipping name and address, contact phone and email. Review package requirements including items to be picked, quantities, special handling needs. Note shipping service level - standard ground, expedited, international. Check for special instructions like gift wrapping, fragile handling, or specific packing requirements. Verify the origin warehouse matches your facility.

                If any information is missing or unclear, coordinate with the team member who created the order or contact customer service for clarification before proceeding.

                Picking Process:
                Picking is selecting the correct items from inventory to fulfill the order. Best practices include using the order details as your picking list, verifying item numbers, descriptions, and quantities exactly, checking item condition - don't ship damaged inventory, using scanning systems if available to ensure accuracy, organizing picked items to prevent mix-ups if processing multiple orders, and double-checking quantities before moving to packing.

                Picking errors are the primary cause of incorrect item exceptions. Take time to verify you have the right items in right quantities.

                Packing for Safe Transit:
                Proper packing prevents damage during shipping and reduces delivery exceptions. Select appropriate box size - not too large causing items to shift, not too small causing crushing. Use adequate cushioning material - bubble wrap, packing peanuts, or air pillows around items and filling empty space. Protect fragile items with extra padding and mark boxes as fragile. Pack heavier items at bottom, lighter items on top. Seal boxes securely with quality packing tape, including reinforcing bottom seams. For valuable or fragile items, consider double-boxing.

                Your packing quality directly affects whether items arrive undamaged. Don't rush this critical step.

                Generating Shipping Labels:
                SLMS integrates with carrier APIs to generate shipping labels. From the order detail page, select Generate Label function. Choose the carrier based on shipping method and destination. Verify the destination address is correct - this is printed on the label. Confirm package weight and dimensions match your actual packed box. Click Generate to create the label. The system sends details to the carrier API and receives a PDF label. Print the label clearly on label paper or regular paper that you'll affix securely.

                The shipping label contains the destination address, return address, tracking barcode, and carrier routing information. This label is essential for delivery - without it, the package cannot be shipped.

                Attaching Labels and Documentation:
                Affix the shipping label prominently on the package top or largest side. Ensure the barcode is flat and readable - no wrinkles or folds across it. Remove or cover any old shipping labels or barcodes that might confuse carriers. For international shipments, attach commercial invoices in clear pouches on the package exterior. Include packing lists inside the box. Add any special handling labels like Fragile, This Side Up, or Keep Dry.

                Labels must be secure enough to remain attached throughout transit and scanning.

                Uploading Order Documents:
                Upload relevant documents to the order record in SLMS. The shipping label you generated is usually auto-uploaded. You may need to upload commercial invoices for international orders, packing lists showing package contents, photos of packages for high-value items, or special documentation for regulated items. Navigate to the order's Documents section, click Upload, select the file and document type, and save.

                Proper documentation supports customer service if issues arise and meets regulatory requirements for international shipping.

                Updating Order Status:
                As you complete processing stages, update the order status to maintain accurate tracking. When you've finished picking, packing, and labeling the package, update status to READY_FOR_PICKUP. This signals the package is complete and staged for carrier pickup. Add notes about any special circumstances like delayed processing, substituted materials, or unusual packing requirements.

                Status updates trigger customer notifications and update tracking information. Keep statuses current to provide accurate visibility.

                Staging for Carrier Pickup:
                Organize READY_FOR_PICKUP packages in your designated pickup area. Group packages by carrier if you use multiple carriers. Keep packages organized and accessible for quick carrier loading. Ensure labels are visible so carriers can scan and verify. Protect staged packages from damage - don't stack too high, keep away from hazards. Maintain staging area security - prevent unauthorized access to packages.

                Well-organized staging speeds carrier pickup and prevents packages from being overlooked.

                Carrier Pickup and Handoff:
                When carriers arrive for scheduled pickups, verify they're collecting the correct packages for their route. Scan or log which packages are being picked up. Update order status to COLLECTED for all picked-up packages. Get carrier confirmation or manifest showing what they took. Report any packages the carrier couldn't accept and investigate the reason.

                The COLLECTED status update triggers the handoff from warehouse responsibility to carrier responsibility.

                Handling Special Order Types:
                Some orders require special handling. For international shipments, ensure all customs documentation is complete and accurate, verify harmonized tariff codes if required, attach commercial invoices prominently, and use appropriate international shipping materials. For expedited orders, prioritize these in your queue, use faster processing, coordinate with carriers for priority pickup, and verify you're using the correct expedited service level. For high-value items, use extra packing protection, photograph the packaged item, consider signature-required service, and potentially insure for full value. For hazardous materials, follow all safety and regulatory requirements, use approved packaging and labeling, ensure proper documentation, and coordinate with carriers certified for hazmat.

                Special handling prevents delays and ensures compliance with regulations.

                Inventory Management:
                As you pick items for orders, your actions may impact inventory tracking. If your system has automated inventory, scanned picks update stock levels automatically. For manual tracking, record what items and quantities were picked. Alert management when inventory is low on frequently ordered items. Report inventory discrepancies if physical stock doesn't match system records. Maintain organized inventory storage for efficient picking.

                Accurate inventory prevents backorders and ensures you can fulfill future orders.

                Quality Control:
                Implement quality checks at key points. Verify picked items match order before packing. Inspect item condition - don't ship damaged goods. Check packing quality - is the box secure and items protected? Verify label accuracy - correct address, tracking barcode scannable. For high-value orders, consider having a second person verify. Random quality audits on a percentage of orders can catch systemic errors.

                Quality control reduces exceptions and increases customer satisfaction.

                Handling Returns:
                When customer service coordinates returns, warehouse receives packages coming back. Receive and log the return with its return authorization number. Inspect returned items for condition. Update SLMS to confirm return receipt. Process items per instructions - restock if resalable, discard if damaged, hold for carrier claims if needed. Notify customer service of receipt and condition. If items don't match return authorization, coordinate with customer service.

                Proper return handling closes the loop on exception resolution.

                Managing Multiple Warehouses:
                If your organization has multiple warehouse locations, ensure orders are processed at the correct facility. Verify the order's assigned warehouse matches your location. Don't process orders meant for other warehouses. Coordinate with other warehouses on inventory transfers if needed. Report if orders are incorrectly routed to your location.

                Correct warehouse assignment ensures efficient fulfillment and accurate inventory.

                Performance Metrics:
                Warehouse performance is measured through various metrics. Order processing time - how long from CREATED to READY_FOR_PICKUP. Accuracy rate - percentage of orders picked and packed correctly. Damage rate - percentage of orders arriving damaged due to packing. Throughput - orders processed per day or hour. On-time staging - percentage of orders ready by carrier pickup time.

                Understanding these metrics helps you identify improvement opportunities and maintain high performance.

                Best Practices for Warehouse Operations:
                Process orders promptly - reduce time in CREATED status. Verify accuracy at every step - picking, packing, labeling. Pack carefully to prevent transit damage. Keep the staging area organized. Update statuses immediately when completed. Communicate with customer service about issues or delays. Maintain clean, organized workspace. Follow safety protocols for lifting and handling. Scan barcodes when available to reduce errors. Double-check addresses before finalizing labels.

                Efficient, accurate warehouse operations are the foundation of successful delivery. Your work directly impacts whether customers receive the right items, undamaged, on time. Take pride in quality fulfillment that sets up the rest of the logistics chain for success.
                """,
                null
        );
    }

    private UpsertDocumentRequest createDocumentManagementDoc() {
        return new UpsertDocumentRequest(
                "Document Management for Warehouse Operations",
                Set.of("WAREHOUSE_STAFF", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Document management is a critical warehouse responsibility in SLMS. Shipping documents enable carrier delivery, meet regulatory requirements, provide proof of shipment, and support customer service when issues arise. This guide covers how warehouse staff manage documents including generation, upload, quality control, and best practices.

                Why Warehouse Document Management Matters:
                Warehouse staff are responsible for ensuring every order has complete, accurate documentation before it ships. Missing or incorrect documents cause delivery delays, customs problems for international shipments, customer service issues, and carrier acceptance rejections. Proper document management prevents these problems and ensures smooth order flow.

                Your documentation work creates the paper trail that follows packages throughout their journey and provides evidence if disputes or questions arise later.

                Types of Documents Warehouse Manages:
                You'll work with several document types. Shipping labels are the most critical - these contain destination address, return address, tracking barcode, and carrier routing information. Commercial invoices for international shipments declare contents, values, and customs information. Packing lists detail items included in the package. Proof of shipment confirms what was sent and when. Customs forms for international or regulated shipments. Special handling documentation for hazardous materials or other special requirements.

                Each document type serves specific purposes and may be required or optional depending on shipment characteristics.

                Generating Shipping Labels:
                Shipping label generation is typically integrated with carrier APIs in SLMS. When you're ready to ship an order, access the order detail page and select Generate Label or similar function. Choose the appropriate carrier based on shipping method and customer requirements. Verify package details including weight, dimensions, and destination address. The system sends this information to the carrier API which generates a compliant label. You receive a PDF that you print and affix to the package.

                Generated labels are automatically uploaded to the order's documents in SLMS, so you don't need to manually upload them.

                Label Quality Requirements:
                Shipping labels must meet carrier requirements for acceptance. Print labels clearly at high resolution - barcodes must be scannable. Use label paper if available, or regular paper with clear tape protection. Ensure no smudging, fading, or print errors that make text or barcodes unreadable. Check that all required fields are present - destination address, return address, tracking number, carrier routing codes. Verify barcodes are straight and unobstructed - no wrinkles or tape across the barcode.

                If a label prints poorly, regenerate it rather than using a questionable label that might cause scanning problems.

                Creating Commercial Invoices:
                International shipments require commercial invoices for customs clearance. Some SLMS configurations can generate these automatically from order data. Others require manual creation. Commercial invoices must include detailed item descriptions - not just "merchandise" but specific product descriptions, harmonized tariff codes for customs classification, declared values in appropriate currency, quantity and unit of measure for each item, shipper and consignee information, reason for export such as sale, gift, sample, return, country of origin for items.

                If you're unsure about tariff codes or values, coordinate with customer service or your warehouse manager.

                Uploading Documents to Orders:
                To upload documents manually, open the order detail page in the admin console. Navigate to the Documents section. Click Upload Document or similar button. Select the document file from your computer - SLMS typically accepts PDF, PNG, and JPEG formats. Choose the document type from the dropdown such as commercial invoice, packing list, photo, or other. Optionally add a description explaining what the document is. Click Upload to attach it to the order.

                The document is now associated with this order and accessible by customer service, customers, and authorized users.

                When to Upload Documents:
                Upload timing depends on document type. Commercial invoices should be uploaded before updating order status to READY_FOR_PICKUP - they're required for shipment. Packing lists can be uploaded after packing if you generate them during that stage. Photos of packages for high-value items should be uploaded immediately after photographing. Special documentation should be uploaded as soon as created.

                Get in the habit of uploading all documents before marking orders READY_FOR_PICKUP to ensure nothing is forgotten.

                Document Organization and Naming:
                Use clear, consistent file names that indicate what the document is. Good examples include Order-12345-Commercial-Invoice.pdf, Order-12345-Packing-List.pdf, or Package-Photo-Order-12345.jpg. Poor examples are Invoice.pdf, Document1.pdf, or IMG_4352.jpg which don't indicate content.

                Clear naming helps anyone who needs to find specific documents later.

                Document Quality Control:
                Before uploading documents, verify quality. Ensure documents are complete - all required fields filled in. Check for accuracy - addresses, values, descriptions match order details. Verify legibility - text and barcodes are clear and readable. Confirm format compliance - documents meet carrier or regulatory requirements. For scanned documents, check scan quality - no missing edges, good resolution.

                Poor quality documents cause problems during shipping or customs clearance. Take time to ensure documents are correct.

                Required vs. Optional Documents:
                Understand which documents are required vs. optional. Always required includes shipping labels for all orders and commercial invoices for international shipments. Often required includes customs forms for some international destinations and hazmat documentation for hazardous materials. Optional but recommended includes packing lists for customer reference, photos for high-value items as proof of condition, and special documentation for unusual shipments.

                If you're unsure whether a document is required, check with your supervisor or customer service.

                Handling Missing or Incorrect Documents:
                If you notice an order is missing required documents or documents are incorrect, don't proceed with shipping until resolved. For missing commercial invoices on international orders, contact customer service to create it or create it yourself if trained. For incorrect addresses on labels, regenerate the label with correct information - don't manually correct printed labels. For missing customs forms, coordinate with compliance team or customer service.

                Never ship orders with missing required documents. Carriers may reject them or customs may delay them significantly.

                Document Access and Privacy:
                Be aware that documents you upload can be accessed by others. Customers can download their order documents through the customer portal. Customer service representatives can access all documents for orders they're managing. Warehouse staff at your facility can typically see all order documents. Administrators have full document access. External auditors may access documents for compliance reviews.

                Don't include sensitive information in documents beyond what's required for shipping and compliance.

                International Shipping Documentation:
                International shipments require special attention to documentation. Commercial invoices are legally required - they must accurately declare contents and values. Harmonized tariff codes must be correct for customs processing. Values must be in the correct currency. Some countries require additional forms like certificates of origin. Underdeclaring values to reduce duties is illegal and can result in seizure.

                If you're unfamiliar with international documentation requirements, get training or work with experienced staff until you're confident.

                Document Retention:
                Documents you upload to SLMS are retained with order records. They remain accessible even after orders are delivered and closed. This long-term retention supports customer service for questions about old orders, accounting and financial auditing, compliance with regulations requiring document retention, and legal requirements for proof of shipment.

                You don't need to separately archive documents you upload to SLMS - the system handles retention automatically.

                Digital vs. Physical Documents:
                SLMS stores documents digitally, but you still work with physical documents in the warehouse. Print shipping labels to affix to packages. Print commercial invoices for international shipment pouches. Potentially include printed packing lists inside packages. Upload digital versions of these documents to SLMS for system records.

                This dual approach ensures physical documents travel with packages while digital copies are available for reference.

                Troubleshooting Document Issues:
                Common document issues and solutions include labels that won't print - check printer connection, paper, and try regenerating the label. Barcode scanning errors - regenerate label or verify printer quality settings. Upload errors - check file format, size limits, and internet connection. Missing documents in the system - verify they were actually uploaded and associated with correct order.

                If you can't resolve document issues, escalate to your IT support or warehouse manager.

                Document Templates and Standards:
                Your organization may have standard templates for documents like packing lists or special forms. Use these templates consistently rather than creating custom formats. Templates ensure required information is included, provide professional appearance, meet carrier and regulatory requirements, and enable consistency across orders.

                If you need a template that doesn't exist, work with your manager to create a standard version rather than improvising.

                Best Practices for Warehouse Document Management:
                Generate and upload all required documents before marking orders READY_FOR_PICKUP. Verify document accuracy - addresses, values, descriptions. Use clear file names indicating document type and order. Check document quality - legibility, completeness, format compliance. Print labels and invoices clearly at good resolution. Organize physical documents with packages to prevent mix-ups. Don't ship orders with missing or incorrect documents. Report recurring document issues to your supervisor. Keep your workspace organized so documents don't get lost or damaged.

                Excellent document management supports smooth shipping, prevents delays and exceptions, ensures regulatory compliance, and provides the information trail needed when issues arise. Your attention to documentation quality directly impacts customer satisfaction and operational efficiency.
                """,
                null
        );
    }

    private UpsertDocumentRequest createWarehouseStatusUpdatesDoc() {
        return new UpsertDocumentRequest(
                "Warehouse Status Updates - Keeping Orders Current",
                Set.of("WAREHOUSE_STAFF", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Updating order statuses is a critical warehouse responsibility in SLMS. Status updates provide real-time visibility into order progress for customers, customer service, and management. They trigger notifications, enable tracking, and create audit trails. This guide covers when and how to update statuses, what each status means, and best practices for maintaining accurate order tracking.

                Why Status Updates Matter:
                When you update an order's status, several important things happen. Customers receive notifications about their order progress. Tracking timelines are updated with the new status and timestamp. Customer service can see current order state to answer inquiries. Management dashboards reflect current warehouse workload and performance. The system creates an audit trail of order progress.

                Timely, accurate status updates are as important as the physical work itself. They provide visibility and transparency throughout the logistics chain.

                Order Statuses Warehouse Manages:
                Warehouse staff are primarily responsible for three order statuses. CREATED is the initial status when an order is first entered - it's awaiting warehouse processing. READY_FOR_PICKUP means you've completed all fulfillment work and the package is staged for carrier pickup. COLLECTED indicates the carrier has physically picked up the package from your warehouse.

                Understanding what each status means ensures you update them correctly.

                CREATED Status - New Orders:
                Orders enter your queue in CREATED status. This means warehouse staff have recorded order details, the order is in the system and has a tracking number, but physical fulfillment hasn't started yet. The package hasn't been picked, packed, or prepared.

                You typically don't need to set CREATED status - it's the initial status when orders are created. Your job is to process these orders and move them to READY_FOR_PICKUP.

                Updating to READY_FOR_PICKUP:
                You update orders to READY_FOR_PICKUP when all warehouse fulfillment is complete. This means you've picked the correct items from inventory, packed them securely in appropriate packaging, generated and printed the shipping label, affixed the label and any required documentation to the package, uploaded any required documents to SLMS, and staged the package in the pickup area for carrier collection.

                READY_FOR_PICKUP tells everyone the package is completely prepared and just waiting for the carrier to take it.

                How to Update Status to READY_FOR_PICKUP:
                Open the order detail page in the admin console. Look for the Status field or Status Update section. Select READY_FOR_PICKUP from the dropdown or click the appropriate status button. Optionally add notes explaining anything unusual about the order processing, special packing used, delays encountered, or items substituted. Click Save or Update to commit the status change.

                The system immediately updates the status, records the timestamp, logs your user ID as who made the change, and triggers customer notifications if configured.

                When to Update to READY_FOR_PICKUP:
                Update status immediately when the package is actually ready - don't update prematurely. Don't update before packing is complete or before labels are attached. Update in real-time as you finish orders rather than batching updates at end of day.

                Real-time updates provide accurate tracking information to customers and prevent confusion about which orders are actually ready for pickup.

                Updating to COLLECTED:
                You update orders to COLLECTED when the carrier has physically taken possession of packages. This happens when carriers arrive for their scheduled pickup, they load packages into their vehicle, you verify which packages they took, and you scan or record the packages leaving your warehouse.

                COLLECTED status triggers the handoff from warehouse responsibility to carrier responsibility.

                How to Update Status to COLLECTED:
                After carrier pickup, update all orders that were picked up to COLLECTED status. You can do this individually or in bulk if you're updating many orders. For individual updates, open each order and change status to COLLECTED with notes about which carrier picked it up and approximate pickup time. For bulk updates, use bulk status update features if available to change multiple orders at once.

                COLLECTED status is critical because it starts the package's journey through the carrier network and triggers customer notifications that their package has shipped.

                Timing of COLLECTED Updates:
                Update orders to COLLECTED as soon as possible after carrier pickup - ideally within minutes to an hour. Don't wait until end of day to update - customers are waiting for shipment confirmation. If carriers do multiple pickups per day, update after each pickup run.

                Prompt COLLECTED updates ensure customers know their packages have shipped and can begin tracking transit progress.

                Carrier Pickup Verification:
                Before updating to COLLECTED, verify the carrier actually took the packages. Get carrier manifest or scan confirmation. Verify packages were loaded onto carrier vehicle. Check that the right carrier took the right packages - don't let carrier A take carrier B's packages.

                Only update to COLLECTED for packages that were definitely collected. If a package was supposed to be picked up but wasn't, leave it in READY_FOR_PICKUP and investigate why.

                Handling Orders That Aren't Picked Up:
                If carriers miss packages during pickup or reject packages for issues, don't update them to COLLECTED. Leave them in READY_FOR_PICKUP status. Add notes explaining why they weren't picked up such as carrier didn't have space, package rejected due to labeling issue, package damaged and needs repacking, or wrong carrier arrived. Contact your supervisor or customer service to coordinate resolution.

                These unpicked packages need investigation and action, not status updates pretending they were collected.

                Status Update Notes and Documentation:
                When updating statuses, add notes when relevant. For READY_FOR_PICKUP, note special handling like items substituted with customer approval, delayed due to inventory issues, or expedited processing for rush order. For COLLECTED, note carrier name and approximate pickup time, any discrepancies like carrier took only some packages, or issues during pickup. Good notes help customer service answer questions and provide context if problems arise.

                Notes become part of the permanent order record and are valuable for audits and issue investigation.

                Status Updates and Notifications:
                Be aware that status updates trigger customer notifications. READY_FOR_PICKUP or COLLECTED typically sends shipping confirmation emails or SMS to customers. Updates inform customers their order is progressing. Notifications include tracking number and current status.

                Since customers receive these notifications, ensure your status updates are accurate and timely.

                Intermediate Statuses:
                Some SLMS configurations might have intermediate statuses beyond the main three. For example, PICKING might indicate the order is being picked from inventory. PACKING might show the order is being packed. QUALITY_CHECK might indicate final verification before staging.

                If your system uses these intermediate statuses, update them as you progress through each stage. They provide even more granular tracking visibility.

                Status Update Errors:
                If you update a status incorrectly, correct it immediately. If you marked an order READY_FOR_PICKUP prematurely before it's actually ready, update notes to clarify and finish preparation quickly. If you marked orders COLLECTED that weren't actually picked up, correct the status back to READY_FOR_PICKUP and notify your supervisor. For wrong carrier noted, update notes with correct information.

                Mistakes happen - what matters is catching and correcting them promptly.

                Bulk Status Updates:
                For high-volume warehouses, updating statuses individually for hundreds of orders is impractical. Use bulk update features if available. Select multiple orders using filters or checkboxes. Change all selected orders to new status with a single action. Add batch notes applicable to all like "Carrier A 2PM pickup June 15".

                Bulk updates enable efficiency while maintaining accuracy.

                Status Updates and Warehouse Performance:
                Your status update timing affects warehouse performance metrics. Time from CREATED to READY_FOR_PICKUP measures warehouse processing efficiency. Time from READY_FOR_PICKUP to COLLECTED shows staging and carrier coordination. Delays in status updates make metrics inaccurate and prevent real-time performance tracking.

                Timely updates provide accurate data for operational improvement.

                Coordinating Status Updates Across Shifts:
                In warehouses with multiple shifts, ensure status updates don't fall through cracks during handoffs. Outgoing shift updates all processed orders before leaving. Incoming shift verifies order statuses match actual warehouse state. Handoff notes communicate what's ready for pickup or waiting for carrier.

                Clear communication between shifts prevents orders from being forgotten or double-counted.

                Status Updates for Problem Orders:
                Some orders have special situations affecting status. For damaged inventory discovered during picking, hold the order in CREATED while sourcing replacement inventory. For missing items, coordinate with supervisors and customer service before updating status. For address validation failures, pause at CREATED until customer service confirms correct address.

                Don't update problem orders through normal status workflow until issues are resolved.

                Audit Trail and Accountability:
                Every status update creates an audit record including new status, timestamp of change, user ID of who made the change, and any notes added. This audit trail provides accountability - you can see who did what and when. This supports quality control, performance review, and issue investigation.

                Knowing your updates are logged should encourage accuracy and timeliness.

                Best Practices for Status Updates:
                Update statuses in real-time as work completes, not in batches at day-end. Ensure status matches reality - don't mark READY_FOR_PICKUP unless actually ready. Add notes for anything unusual or important. Verify carrier pickups before updating to COLLECTED. Use bulk updates efficiently for high volume. Coordinate status updates during shift changes. Correct mistakes immediately when discovered. Understand that customers see these status changes.

                Accurate, timely status updates create transparency and trust throughout the logistics chain. Your updates enable customers to track their packages, customer service to answer questions, and management to optimize operations. Treat status updates as an essential part of order fulfillment, not an administrative afterthought.
                """,
                null
        );
    }

    // ================================
    // ADMINISTRATION DOCUMENTS
    // ================================

    private UpsertDocumentRequest createUserManagementDoc() {
        return new UpsertDocumentRequest(
                "User Management for Administrators",
                Set.of("ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                User management is a core administrative responsibility in SLMS. As an administrator, you control who has access to the system, what permissions they have, and how they interact with logistics operations. Effective user management ensures security, enables appropriate access for different roles, maintains accountability, and supports team operations. This comprehensive guide covers user creation, role assignment, permission management, and best practices.

                Why User Management Matters:
                Proper user management is critical for several reasons. Security - only authorized individuals can access the system and sensitive data. Role-based access - users have permissions appropriate for their responsibilities. Accountability - all actions are logged to specific user accounts. Operational efficiency - users can access the tools they need without barriers. Compliance - user access controls meet regulatory and audit requirements.

                As an administrator, you're the guardian of system access and security.

                User Roles in SLMS:
                SLMS implements role-based access control with distinct user types. CUSTOMER users are order recipients who track shipments and report issues. DRIVER users are delivery personnel who confirm deliveries via QR scanning. WAREHOUSE_STAFF users process orders and manage fulfillment. CUSTOMER_SERVICE_REP users handle delivery exceptions and customer support. ADMIN users have full system access for management and configuration.

                Each role has specific permissions designed for their responsibilities.

                Creating New Users:
                To create a new user account, navigate to User Management section in the admin console. Click Add New User or Create User button. Enter required user information including full name, email address which serves as username, phone number for contact, assigned role from the dropdown, and optionally any organizational details like department or employee ID. The system generates initial login credentials - you'll provide these to the new user. Save to create the account.

                The new user can now log in with their email and initial password. They'll typically be prompted to change their password on first login.

                Assigning User Roles:
                When creating or editing users, select the appropriate role carefully. Consider what the user needs to accomplish - customers track orders, drivers confirm deliveries, warehouse staff process shipments, customer service reps handle exceptions, administrators manage the system. Match role to minimum necessary permissions - don't give admin access when warehouse staff role is sufficient. Remember roles determine what features and data users can access.

                Roles can be changed later if user responsibilities change.

                Managing Customer Accounts:
                Customer accounts are typically created when customers register through the customer portal. As an administrator, you can also create customer accounts manually for special situations. You might create accounts for VIP customers, business account managers, or imported customer lists from other systems.

                Customer accounts are linked to orders by email or customer ID. Ensure customers can see their order history by verifying the email on orders matches their account email.

                Managing Driver Accounts:
                Create driver accounts for delivery personnel who need to confirm deliveries. Driver accounts need minimal information - name, email, phone number, and DRIVER role assignment. Drivers use their accounts to access the driver PWA on mobile devices. Their credentials are tied to delivery confirmations for accountability.

                Ensure drivers keep their credentials secure and don't share logins.

                Managing Warehouse Staff Accounts:
                Create warehouse staff accounts for team members who fulfill orders. These users access the admin console to create orders, update statuses, upload documents, and manage warehouse operations. You might assign different warehouse staff to different warehouse locations if you operate multiple facilities.

                Monitor warehouse staff accounts for unusual activity or errors that might indicate training needs.

                Managing Customer Service Representative Accounts:
                Create customer service rep accounts for support team members. These users access exception management, customer communication tools, order and customer data for support, and reporting dashboards. Customer service reps need broader data access than other roles to investigate issues across all customers and orders.

                Review customer service representative access periodically to ensure only current team members have accounts.

                Managing Administrator Accounts:
                Administrator accounts have full system access. Grant admin role only to trusted individuals who need comprehensive access for system management, configuration, user administration, or oversight responsibilities.

                Limit the number of administrator accounts to reduce security risk. Not everyone in management needs admin access - many can function with customer service rep or warehouse staff roles depending on their actual needs.

                Password Management:
                Enforce strong password policies. Require minimum password length such as 8-12 characters, complexity including uppercase, lowercase, numbers, and symbols, and password expiration such as every 90 days for sensitive accounts. Enable password reset capabilities so users can reset forgotten passwords via email. Never share passwords or store them in plain text.

                SLMS may handle password policies automatically through integration with authentication systems like Keycloak.

                Updating User Information:
                Update user records when information changes. To update a user, open their account in User Management. Edit fields like name, email, phone number, role assignment, or account status. Save changes to commit.

                Keep user records current to ensure accurate contact information and appropriate access.

                Deactivating Users:
                When users no longer need access, deactivate their accounts rather than deleting them immediately. Deactivated accounts can't log in but retain historical data about their actions for audit trails.

                Deactivate accounts when employees leave the company, contractors complete their work, customers close their accounts upon request, or access is no longer needed for any reason.

                Deleting User Accounts:
                Deletion permanently removes user accounts and should be rare. Consider deleting accounts only when required for privacy regulations like GDPR right to be forgotten, duplicate accounts that were created in error, or test accounts after testing is complete.

                Before deletion, ensure no critical data will be lost. Order history and audit logs should be preserved even when user accounts are deleted.

                Resetting Passwords:
                Users occasionally need password resets due to forgetting passwords or security concerns. To reset a user's password, access their account in User Management. Select Reset Password or similar function. Generate a new temporary password or send a reset link to their email. Provide the temporary password or confirm they received the reset email. Instruct them to change the password immediately upon login.

                Don't send passwords via insecure channels. Use the system's built-in password reset mechanisms.

                Auditing User Activity:
                Monitor user activity through audit logs. Review who is accessing the system and when. Check for unusual activity like login attempts from unexpected locations, access outside normal hours, or multiple failed login attempts suggesting password attacks. Investigate suspicious patterns. Verify that user actions match their role - for example, warehouse staff shouldn't be accessing exception management, customers shouldn't see other customers' orders.

                Regular audit reviews help detect security issues and inappropriate access.

                Managing Permissions and Access Control:
                Beyond role assignment, some systems allow granular permission control. You might restrict access to specific warehouses, limit access to certain customers or accounts, control access to financial or sensitive data, or enable special features for specific users.

                Use granular controls when needed but maintain simplicity where possible. Overly complex permission schemes become difficult to manage and audit.

                Handling Access Requests:
                Users may request access changes like additional permissions, role upgrades, access to new features, or restoration of deactivated accounts. Evaluate requests based on business need - does the user actually need this access for their job? Security impact - does granting access create risks? Approval authority - is this request within your authority or does it need higher approval?

                Document approval and justification for significant access grants.

                User Onboarding:
                When creating accounts for new employees or users, provide proper onboarding. Create their account with appropriate role. Send credentials securely. Provide training or documentation on how to use SLMS for their role. Verify they can log in successfully. Answer initial questions about system use.

                Good onboarding helps users become productive quickly and reduces support burden.

                User Offboarding:
                When users leave, ensure proper offboarding. Deactivate or delete their account promptly. Ensure no critical processes depend on their personal account. Transfer ownership of any work items or assignments to other users. Document the offboarding for audit purposes.

                Prompt offboarding prevents unauthorized access from former employees.

                Multi-Factor Authentication:
                If SLMS supports multi-factor authentication (MFA), consider requiring it for sensitive accounts like administrators, customer service reps with broad data access, or users accessing from untrusted networks.

                MFA significantly improves security by requiring a second factor beyond just password.

                Single Sign-On Integration:
                SLMS may integrate with enterprise single sign-on systems like Keycloak. This allows users to log in with corporate credentials, reduces password fatigue, centralizes access management, and improves security through enterprise authentication policies.

                If SSO is available, leverage it for enterprise user management.

                Best Practices for User Management:
                Grant minimum necessary permissions - don't over-privilege users. Review user accounts regularly - deactivate accounts that are no longer needed. Enforce strong password policies. Monitor audit logs for suspicious activity. Document access approval justifications for sensitive access. Train users on security best practices like not sharing passwords. Respond promptly to access requests and password resets. Maintain clean user records with current information. Separate administrative accounts from regular accounts for administrators who also do operational work.

                Effective user management balances security, usability, and operational needs. As an administrator, your careful management of user access protects SLMS while enabling users to do their jobs efficiently. Treat user management as an ongoing responsibility, not a one-time setup task.
                """,
                null
        );
    }

    private UpsertDocumentRequest createWarehouseAdminDoc() {
        return new UpsertDocumentRequest(
                "Warehouse Administration and Configuration",
                Set.of("ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Warehouse administration involves configuring and managing warehouse locations in SLMS. For organizations with one or multiple warehouses, proper warehouse configuration ensures orders are routed correctly, inventory is managed appropriately, and fulfillment operations run smoothly. This guide covers warehouse setup, configuration, management, and optimization.

                Why Warehouse Configuration Matters:
                Proper warehouse configuration affects order routing to the optimal fulfillment location, inventory management and allocation, shipping cost calculation based on origin location, performance tracking by warehouse facility, and resource planning and capacity management.

                Well-configured warehouses enable efficient, cost-effective logistics operations.

                Warehouse Information in SLMS:
                Each warehouse location in SLMS has specific configuration including unique warehouse ID or code, warehouse name and description, physical address for shipping origin, contact information including phone and email, operating hours and capacity, assigned warehouse staff users, and fulfillment capabilities and specializations.

                Complete, accurate warehouse information ensures the system can properly manage multi-warehouse operations.

                Creating New Warehouse Locations:
                To add a new warehouse to SLMS, navigate to Warehouse Management section in admin console. Click Add Warehouse or Create New Warehouse. Enter warehouse details including unique warehouse code such as WH-001, WH-WEST, descriptive name like Main Distribution Center or West Coast Warehouse, complete physical address for return address and origin calculations, contact phone and email, operating hours if relevant for pickup scheduling, and initial capacity settings.

                Save to create the warehouse record. You can then assign staff and configure additional settings.

                Warehouse Address Configuration:
                The warehouse address is critical because it appears as return address on shipping labels, is used for shipping cost calculations as package origin, determines service levels and transit times to destinations, and affects carrier rate shopping.

                Ensure warehouse addresses are complete and correctly formatted for carrier systems. Verify address with carrier address validation if available.

                Assigning Users to Warehouses:
                If you operate multiple warehouses, assign warehouse staff users to their respective locations. Edit user accounts to set their assigned warehouse. Configure permissions so users can only see and process orders for their warehouse if isolation is desired. Enable cross-warehouse access for managers who oversee multiple facilities.

                Proper user-to-warehouse assignment prevents staff from accidentally processing orders meant for different locations.

                Warehouse Capacity Management:
                Configure warehouse capacity settings to prevent overload. Set maximum daily order capacity based on staffing and equipment. Define peak capacity for busy periods. Configure workload balancing rules if SLMS supports automatic order routing.

                Capacity management helps logistics managers route orders appropriately and identify when to scale resources.

                Multi-Warehouse Order Routing:
                For organizations with multiple warehouses, configure how orders are routed to fulfillment locations. Options might include route to nearest warehouse based on destination to minimize shipping time and cost, route based on inventory availability to the warehouse that has items in stock, route based on workload to balance across facilities, or manual assignment where someone explicitly assigns orders to warehouses.

                Effective routing reduces shipping costs, speeds delivery, and balances workload across facilities.

                Warehouse Specialization:
                Some warehouses may specialize in certain types of orders. Configure specialization settings such as international shipping warehouse with customs expertise, hazmat warehouse certified for dangerous goods, cold storage warehouse for temperature-sensitive items, or high-value items warehouse with enhanced security.

                Specialization routing ensures orders go to warehouses equipped to handle them properly.

                Warehouse Performance Metrics:
                Track performance metrics by warehouse to identify top performers and areas needing improvement. Key metrics include order processing time from CREATED to READY_FOR_PICKUP, accuracy rate showing percentage of correct orders, damage rate indicating packing quality, throughput measuring orders processed per day or hour, and on-time performance showing orders ready by carrier pickup.

                Compare metrics across warehouses to share best practices and target interventions.

                Managing Warehouse Operating Hours:
                Configure operating hours for each warehouse. Set regular business hours for normal operations. Define extended hours for peak seasons. Specify holiday closures when warehouse is not operating. Configure weekend availability if applicable.

                Operating hours affect when orders can be processed and when carriers can pick up, influencing delivery timeframes.

                Warehouse Inventory Management:
                If SLMS integrates with inventory management systems, configure the integration for each warehouse. Link warehouse to inventory system. Map SLMS warehouse codes to inventory system identifiers. Configure inventory synchronization frequency. Set low-stock alert thresholds.

                Integration ensures orders are only routed to warehouses with available inventory.

                Carrier Integration by Warehouse:
                Different warehouses might use different carriers based on location and agreements. Configure carrier relationships by warehouse. Assign primary carriers for each warehouse. Configure rate shopping for cost optimization. Set up carrier pickup schedules specific to each location.

                Warehouse-specific carrier configuration enables optimal shipping for each location's circumstances.

                Warehouse Reporting and Analytics:
                Generate reports analyzing warehouse performance. Compare warehouses on efficiency, accuracy, and throughput. Identify which warehouses handle which order volumes. Analyze shipping costs by warehouse to find optimization opportunities. Track exception rates by warehouse to identify quality issues.

                Data-driven warehouse management enables continuous improvement.

                Adding Warehouse Capacity:
                When adding new warehouse capacity, plan configuration carefully. Determine warehouse purpose and specialization. Hire and train warehouse staff. Set up users and assign to new warehouse. Configure capacity and operating hours. Establish carrier relationships and pickups. Test with sample orders before going live. Gradually ramp up volume to the new warehouse.

                Careful rollout prevents disruption and ensures new warehouses are properly integrated.

                Decommissioning Warehouses:
                When closing a warehouse, ensure orderly decommissioning. Stop routing new orders to the closing warehouse. Complete all in-progress orders. Deactivate or reassign warehouse staff users. Update system configuration to remove warehouse from active locations. Archive warehouse data for historical records. Document closure for audit trail.

                Proper decommissioning prevents orders from being routed to non-operational facilities.

                Warehouse Address Changes:
                If a warehouse relocates, update addresses carefully. Schedule address change to minimize disruption. Update warehouse address in SLMS configuration. Verify address change with carriers. Communicate change to all stakeholders. Update any integrations or external systems referencing the old address. Test that new address works correctly for label generation and shipping.

                Address changes affect shipping labels and must be managed carefully.

                Warehouse Naming and Identification:
                Use clear, consistent warehouse naming conventions. Good examples include geographic names like East Coast DC or Dallas Warehouse, functional names like International Shipping Center, or code-based names like WH-001, WH-002.

                Avoid ambiguous names that make it unclear which facility is which.

                Warehouse Documentation:
                Maintain documentation for each warehouse including configuration settings and changes, standard operating procedures, staff assignments and contact information, carrier relationships and pickup schedules, specializations and capabilities, performance benchmarks and targets, and escalation procedures for issues.

                Documentation helps train new staff and ensures continuity when personnel changes.

                Warehouse Communication and Coordination:
                Facilitate communication between warehouses for best practice sharing, workload balancing during peak times, inventory transfers if needed, and escalation of complex issues.

                Multi-warehouse organizations benefit from collaboration rather than silos.

                Best Practices for Warehouse Administration:
                Maintain complete, accurate warehouse records. Configure order routing rules thoughtfully to optimize costs and efficiency. Track and compare warehouse performance metrics. Keep warehouse addresses current and verified. Assign users to correct warehouses and maintain proper access controls. Document warehouse configurations and procedures. Plan capacity to handle peak volumes. Communicate configuration changes to affected stakeholders. Test warehouse configurations before going live. Review and optimize warehouse operations periodically.

                Effective warehouse administration ensures your physical logistics operations are properly represented and managed in SLMS. Whether you operate one warehouse or many, configuration quality directly impacts operational efficiency and success.
                """,
                null
        );
    }

    private UpsertDocumentRequest createDashboardStatsDoc() {
        return new UpsertDocumentRequest(
                "Dashboard Statistics and Operational Reporting",
                Set.of("ADMIN", "CUSTOMER_SERVICE_REP"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                SLMS dashboards provide real-time visibility into logistics operations through key performance indicators, metrics, and reports. Understanding how to use dashboards helps administrators and managers monitor performance, identify issues, make data-driven decisions, and drive continuous improvement. This comprehensive guide covers dashboard features, key metrics, and how to leverage operational data.

                Why Dashboards Matter:
                Dashboards transform raw operational data into actionable insights. They provide real-time visibility into what's happening across your logistics operations, identify trends and patterns that might indicate problems or opportunities, enable data-driven decision making rather than gut feelings, measure performance against targets and benchmarks, and support accountability by making performance transparent.

                Regular dashboard review is essential for effective logistics management.

                Dashboard Overview:
                SLMS provides different dashboards for different roles. The admin dashboard shows system-wide operational metrics. The warehouse dashboard focuses on fulfillment performance. The customer service dashboard tracks exception management. The order management dashboard monitors order flow and status.

                Access relevant dashboards from the admin console navigation.

                Key Order Metrics:
                Order-related metrics provide insight into fulfillment volume and flow. Total orders shows count of orders in various time periods like today, this week, this month. Orders by status breaks down current orders by status like CREATED, READY_FOR_PICKUP, IN_TRANSIT, DELIVERED. New orders tracks orders created recently. Delivered orders shows successful completions. Cancelled orders indicates fulfillment failures.

                These metrics help you understand order volume, flow, and completion rates.

                Delivery Performance Metrics:
                Track how well deliveries are performing. Delivery success rate shows percentage of orders successfully delivered vs. total shipped. Average delivery time measures days from order creation to delivery. On-time delivery rate compares actual delivery vs. estimated delivery dates. Failed delivery attempts track delivery problems. Delivery exception rate shows percentage of orders with reported issues.

                These metrics indicate customer satisfaction and carrier performance.

                Warehouse Performance Metrics:
                Monitor warehouse efficiency and quality. Processing time tracks average time from order CREATED to READY_FOR_PICKUP. Order accuracy rate shows percentage of orders picked and packed correctly. Damage rate indicates percentage of orders damaged during warehouse handling or packing. Daily throughput measures orders processed per day. Capacity utilization compares actual vs. maximum daily order capacity.

                Warehouse metrics help identify efficiency improvements and quality issues.

                Exception Management Metrics:
                For customer service oversight, track exception handling. Total open exceptions shows current unresolved issues. Exceptions by status breaks down exceptions by OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED. Average time to resolution measures how long exceptions take to resolve. Exception rate by type shows which issues are most common like damaged packages, incorrect items, non-receipt. Customer satisfaction related to exception resolution.

                Exception metrics help optimize customer service operations and identify systemic problems.

                Driver Performance Metrics:
                If you manage delivery drivers, track their performance. Deliveries per driver shows workload distribution. Delivery confirmation rate indicates percentage confirmed via QR code. Average deliveries per day or hour measures productivity. Failed delivery rate by driver identifies training needs or problem areas.

                Driver metrics support performance management and workload balancing.

                Carrier Performance Metrics:
                Compare carrier performance to optimize carrier selection. Delivery times by carrier compare transit speed. Delivery success rates by carrier show reliability. Damage rates by carrier indicate handling quality. Cost per delivery by carrier enables cost comparison. Exception rates by carrier identify problem carriers.

                Carrier metrics support contract negotiations and carrier selection decisions.

                Time-Based Trending:
                View metrics over time to identify trends. Daily trends show fluctuations and daily patterns. Weekly trends smooth out daily noise to show patterns. Monthly trends reveal seasonal variations. Year-over-year comparisons show growth or decline.

                Trend analysis helps with capacity planning, resource allocation, and identifying improving or degrading performance.

                Real-Time vs. Historical Data:
                Dashboards typically show both real-time current state and historical trends. Real-time data shows current orders, statuses, and active exceptions. Historical data shows past performance, trends, and comparisons.

                Use real-time data for operational monitoring and immediate action. Use historical data for planning and performance analysis.

                Filtering and Segmentation:
                Most dashboards allow filtering to focus on specific segments. Filter by date range to analyze specific time periods. Filter by warehouse to compare locations. Filter by customer, order type, or shipping method to analyze segments. Filter by carrier to evaluate specific carriers.

                Filtering reveals insights that aggregated data might hide.

                Alerts and Notifications:
                Configure alerts for important conditions. Alert when exception counts exceed thresholds. Notify when delivery success rate drops below targets. Alert on unusual spikes or drops in order volume. Notify about warehouse capacity approaching limits.

                Automated alerts enable proactive response to issues before they become serious.

                Custom Reports:
                Beyond standard dashboards, generate custom reports for specific analysis. Order detail reports with complete order information and history. Exception reports with full exception details. Financial reports showing shipping costs and revenue. Compliance reports demonstrating regulatory adherence.

                Custom reports support specific analysis needs and stakeholder communication.

                Exporting Dashboard Data:
                Export dashboard and report data for further analysis. Common export formats include CSV for spreadsheet analysis, PDF for presentation and distribution, and JSON or other formats for integration with external systems.

                Export capabilities allow deeper analysis in tools like Excel or business intelligence platforms.

                Dashboard Best Practices:
                Review dashboards daily to stay on top of operations. Look for anomalies like unusual spikes, drops, or patterns. Drill down into concerning metrics to understand root causes. Compare current performance to historical benchmarks. Share relevant metrics with your team for transparency and accountability. Use insights to drive continuous improvement initiatives. Don't just view data - act on insights to improve performance.

                Dashboards are only valuable if you use them to drive decisions and actions.

                Metric Interpretation:
                Understand what metrics really mean. High exception rates might indicate carrier problems, warehouse quality issues, or unrealistic customer expectations. Increasing delivery times could mean carrier delays, warehouse bottlenecks, or longer-distance orders. Rising costs per delivery might reflect fuel price increases, carrier rate hikes, or inefficient routing.

                Context and investigation are essential for proper metric interpretation.

                Benchmarking:
                Compare your metrics to industry benchmarks or internal targets. Set realistic targets for delivery times, success rates, exception rates, and costs. Measure progress toward targets over time. Celebrate improvements and investigate degradation. Benchmark against competitors if data is available.

                Benchmarking provides context for whether your performance is acceptable or needs improvement.

                Communicating Dashboard Insights:
                Share relevant metrics with stakeholders. Present weekly or monthly performance summaries to leadership. Share warehouse metrics with warehouse managers to drive improvement. Communicate carrier performance to procurement for contract discussions. Report customer service metrics to the support team for coaching.

                Transparent metrics create accountability and focus improvement efforts.

                Dashboard Limitations:
                Remember that dashboards show what is measured, not everything that matters. Quantitative metrics are incomplete without qualitative insights. Average metrics hide outliers and distributions. Correlation doesn't imply causation - investigate apparent relationships. Data quality issues affect metric accuracy.

                Use dashboards as tools for insight, not as the complete truth.

                Best Practices for Using Dashboards:
                Review dashboards regularly at consistent times. Focus on actionable metrics you can influence. Investigate anomalies and trends rather than just noting them. Share insights with relevant teams. Use metrics to celebrate successes and identify improvement opportunities. Verify data quality when metrics seem inconsistent. Balance quantitative metrics with qualitative observations. Take action based on insights - dashboards aren't useful if they don't drive improvement.

                Effective use of SLMS dashboards transforms operational data into competitive advantage. Regular review, thoughtful interpretation, and action based on insights enable continuous improvement that enhances customer satisfaction, reduces costs, and optimizes logistics performance.
                """,
                null
        );
    }

    private UpsertDocumentRequest createSystemOrderManagementDoc() {
        return new UpsertDocumentRequest(
                "System-Wide Order Management for Administrators",
                Set.of("ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                As an administrator, you have comprehensive access to all orders across the entire SLMS system. This system-wide visibility enables you to monitor operations, intervene when needed, resolve escalated issues, generate reports, and ensure smooth logistics operations. This guide covers how administrators use their broad order access for oversight and management.

                Administrative Order Access:
                Unlike other roles with limited order visibility, administrators can view all orders in the system regardless of customer, warehouse, status, or other restrictions. This comprehensive access supports oversight and management, exception escalation and resolution, cross-warehouse coordination, performance analysis and reporting, and audit and compliance activities.

                With this broad access comes responsibility to use it appropriately and maintain data privacy.

                Viewing All System Orders:
                Access the Orders section from the admin console. By default, you see all orders across all customers and warehouses. Use filters to focus on specific subsets like orders from specific warehouses, orders in specific statuses, orders within date ranges, orders for specific customers, or orders with exceptions.

                The ability to see all orders enables comprehensive operational visibility.

                Order Search and Filtering:
                Powerful search capabilities help you find specific orders or analyze patterns. Search by tracking number to pull up specific orders instantly. Search by customer name or email to see a customer's order history. Filter by order status to analyze workflow stages. Filter by shipping method, destination, or warehouse. Combine multiple filters for detailed analysis.

                Effective use of search and filters enables efficient order management despite large order volumes.

                Order Detail Access:
                Click any order to view comprehensive details including all order information like customer, destination, package details, complete tracking timeline with all status changes and timestamps, all associated documents like shipping labels and invoices, delivery confirmation details if delivered, any reported exceptions and their resolution history, and audit trail showing who created the order, updated statuses, and uploaded documents.

                This complete view supports investigation and issue resolution.

                Intervening in Orders:
                Sometimes administrators need to intervene directly in orders. Common interventions include manually updating order status when system issues prevent normal updates, correcting order information like addresses if errors are discovered, cancelling orders when necessary, reassigning orders to different warehouses, or adding notes documenting administrative actions.

                Intervention should be exception rather than routine - don't circumvent normal workflows unnecessarily.

                Handling Escalated Orders:
                Customer service may escalate complex orders to administrators. These might involve high-value shipments requiring special approval, address corrections needing administrative override, system errors affecting order processing, carrier disputes requiring management intervention, or VIP customer situations needing personal attention.

                When handling escalations, document your investigation and actions thoroughly in order notes.

                Order Cancellation:
                Administrators can cancel orders when appropriate. Valid cancellation reasons include customer requests to cancel, inventory unavailability discovered after order creation, address validation failures that can't be resolved, payment issues or fraud concerns, or operational issues preventing fulfillment.

                When cancelling orders, update status to CANCELLED, add detailed notes explaining why, ensure customers are notified, and process any required refunds.

                Cross-Warehouse Order Management:
                With multiple warehouses, you might need to reassign orders between facilities. Reassignment scenarios include initial warehouse can't fulfill due to inventory issues, workload balancing during peak times, customer location changes requiring different origin, or operational issues at assigned warehouse.

                To reassign, update the order's assigned warehouse, notify both warehouse teams, add notes explaining the reassignment, and monitor to ensure the new warehouse processes it.

                Order Data for Reporting:
                Use comprehensive order access to generate reports. Extract order data for specific time periods, customers, or warehouses. Analyze order patterns and trends. Calculate performance metrics. Generate financial reports on shipping costs and revenue. Produce compliance reports for audits.

                Order data is the foundation for operational and financial reporting.

                Resolving Data Quality Issues:
                Administrators often need to clean up order data issues. Fix incorrect or incomplete addresses. Correct tracking number typos or errors. Update order information that was entered incorrectly. Merge duplicate orders created in error. Document corrections in notes.

                Data quality maintenance ensures system accuracy and prevents operational issues.

                Handling System Errors:
                When system errors affect orders, administrators investigate and resolve. Orders stuck in statuses due to bugs. Failed status transitions or API calls. Document upload failures. Integration issues with carriers or inventory systems.

                Technical troubleshooting may require IT support, but administrators coordinate resolution.

                Order Audit and Compliance:
                Use order access for audit and compliance activities. Verify orders follow proper workflows and policies. Investigate suspicious orders or patterns. Ensure documentation meets regulatory requirements. Respond to audit requests with order data and evidence. Monitor for policy violations or fraud.

                Audit activities support risk management and regulatory compliance.

                Bulk Order Operations:
                Administrators may need to perform bulk actions on multiple orders. Bulk status updates for groups of orders. Bulk export for reporting or integration. Bulk assignment to warehouses or staff. Bulk data corrections for systematic issues.

                Bulk operations enable efficient management of large order volumes.

                Order Data Privacy:
                Despite broad access, respect customer data privacy. Access orders only when there's legitimate business need. Don't share customer data outside proper channels. Follow data protection regulations like GDPR. Maintain confidentiality of customer information. Use access appropriately, not for personal curiosity.

                Administrators are trusted with broad access - honor that trust.

                Order History and Archives:
                Administrators can access historical orders beyond normal retention. View orders from months or years ago for customer service needs, legal discovery requests, audit requirements, or historical analysis.

                Old order data supports long-term customer service and compliance.

                Order Import and Export:
                Administrators may import orders from external systems or export for integration. Import orders in bulk from CSV or other formats. Export order data for external systems. Verify data mapping and formatting. Test imports with small samples before bulk loading.

                Order integration capabilities support operational flexibility.

                Order System Configuration:
                Configure system-wide order settings including default order statuses and workflows, required fields for order creation, validation rules for addresses and data, notification triggers and templates, and integration settings with carriers and inventory.

                Configuration affects how orders flow through the system.

                Monitoring Order Flow:
                Track orders through their lifecycle to identify bottlenecks. Monitor time in each status to find delays. Identify where orders get stuck. Analyze why orders aren't progressing. Coordinate with responsible teams to resolve blockages.

                Flow monitoring enables proactive problem-solving.

                Best Practices for Administrative Order Management:
                Use broad access responsibly and only when necessary. Respect customer data privacy despite access privileges. Document all administrative interventions in order notes. Communicate with affected teams when you make changes. Follow proper procedures even though you have override capabilities. Use filters and search effectively to find relevant orders. Generate regular reports to monitor system-wide performance. Escalate technical issues to IT rather than working around them. Maintain audit trails for compliance. Balance oversight with empowering other roles to do their jobs.

                Administrative order management provides the oversight and flexibility needed to ensure SLMS operates smoothly. Use your comprehensive access wisely to support operations, resolve issues, and maintain system integrity while respecting privacy and established workflows.
                """,
                null
        );
    }

    private UpsertDocumentRequest createRolePermissionsDoc() {
        return new UpsertDocumentRequest(
                "Understanding Roles and Permissions in SLMS",
                Set.of("ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                SLMS implements role-based access control where users have different permissions based on their assigned role. Understanding how roles and permissions work helps administrators configure appropriate access, maintain security, enable users to perform their jobs, and comply with data protection requirements. This comprehensive guide explains the permission model and how to manage it effectively.

                Role-Based Access Control Overview:
                Rather than assigning permissions individually to each user, SLMS groups permissions into roles. Users are assigned one or more roles which determine what they can access and do. This approach simplifies administration - assign a role rather than many individual permissions, ensures consistency - all users in a role have the same access, supports audit and compliance through clear access documentation, and enables easy access changes when users change responsibilities.

                Role-based access control is the foundation of SLMS security.

                The Five Primary Roles:
                SLMS defines five primary user roles. ANONYMOUS represents public users who haven't logged in. CUSTOMER represents registered order recipients. DRIVER represents delivery personnel. WAREHOUSE_STAFF represents fulfillment team members. CUSTOMER_SERVICE_REP represents support team members. ADMIN represents system administrators and managers.

                Each role has specific permissions appropriate for its responsibilities.

                ANONYMOUS User Permissions:
                Anonymous users are visitors who haven't logged in. They can access the public landing page and use the public order tracking page by entering a tracking number. They cannot access order history, report exceptions, view customer portals, or access admin features.

                Anonymous access provides basic tracking without requiring account creation.

                CUSTOMER Role Permissions:
                Customers are registered users who receive orders. They can view their own orders only, not other customers' orders. They can track order status and view tracking timelines. They can download shipping documents for their orders. They can report delivery exceptions on their orders. They can manage their account and notification preferences.

                Customers cannot access admin functions, see other customers' data, create orders, or manage system configuration.

                Customer access is limited to their own data for privacy.

                DRIVER Role Permissions:
                Drivers are delivery personnel using the mobile PWA. They can scan QR codes to confirm deliveries. They can view orders assigned to their delivery routes. They can update delivery status to DELIVERED via QR scanning.

                Drivers cannot access order creation, customer service functions, system administration, other drivers' deliveries, or full order history.

                Driver access is minimal and focused on delivery confirmation.

                WAREHOUSE_STAFF Role Permissions:
                Warehouse staff process orders and manage fulfillment. They can view all orders in the system or filtered to their warehouse. They can create new orders in the system. They can update order statuses as they process packages. They can upload documents like shipping labels and invoices. They can generate shipping labels via carrier integration. They can manage warehouse inventory and operations.

                Warehouse staff cannot access customer service exception management, user administration, system configuration, or customer account information beyond what's needed for fulfillment.

                Warehouse staff have operational access but not administrative or customer service access.

                CUSTOMER_SERVICE_REP Role Permissions:
                Customer service representatives handle exceptions and customer support. They can view all orders and customers for support purposes. They can access and manage all delivery exceptions. They can communicate with customers about issues. They can update exceptions through the resolution workflow. They can coordinate with carriers and warehouses. They can view customer service dashboards and metrics.

                Customer service reps cannot create or delete users, change system configuration, access full admin features, or modify warehouse operations.

                Customer service access is broad for support but doesn't include administration or warehouse operations.

                ADMIN Role Permissions:
                Administrators have full system access. They can do everything all other roles can do. They can create, edit, and delete user accounts. They can configure warehouses and system settings. They can access all orders, customers, and data. They can run reports and view all dashboards. They can intervene in any process when needed.

                Administrator access is comprehensive - use it responsibly.

                Permission Principles:
                SLMS follows security best practices in permission design. Principle of least privilege means users get only permissions needed for their job, no more. Role separation means different roles have different access - warehouse staff don't need exception access. Data isolation means users typically see only data relevant to them. Audit logging means all actions are logged to user accounts for accountability.

                These principles balance usability with security.

                Assigning Roles to Users:
                When creating or editing users, assign appropriate roles. Consider what the user needs to accomplish in their job. Match role to responsibilities - don't over-privilege. Assign the most restrictive role that enables their work. Remember roles can be changed if responsibilities change.

                Thoughtful role assignment maintains security while enabling productivity.

                Multiple Role Assignment:
                Some systems allow users to have multiple roles. For example, a warehouse manager might have both WAREHOUSE_STAFF and ADMIN roles. An employee who sometimes helps with customer service might have both their primary role and CUSTOMER_SERVICE_REP.

                If your SLMS supports multiple roles, use this capability sparingly and only when clearly needed.

                Custom Roles:
                Some SLMS implementations support custom roles beyond the five standard roles. Custom roles might include warehouse supervisor with additional warehouse management permissions, customer service supervisor with team management capabilities, or read-only analyst with view access but no modification permissions.

                Custom roles enable fine-tuned access control for specific organizational needs.

                Permission Auditing:
                Regularly audit who has what permissions. Review user roles to ensure they still match job responsibilities. Identify over-privileged users who have more access than needed. Find users who should have been deactivated but still have access. Verify sensitive permissions like ADMIN are appropriately restricted.

                Regular audits maintain security and compliance.

                Permission Requests:
                Users may request additional permissions or role changes. Evaluate requests based on business need, security risk, approval authority, and documentation requirements.

                Document permission grants, especially for elevated access like ADMIN role.

                Revoking Permissions:
                When users no longer need permissions, revoke promptly. When employees leave, deactivate accounts immediately. When contractors complete work, remove their access. When users change roles, update to new appropriate role. When special access was temporary, revoke when no longer needed.

                Timely revocation prevents unauthorized access from former employees or outdated permissions.

                Permission and Compliance:
                Permission management supports compliance with data protection regulations like GDPR, industry standards like PCI DSS for payment data, and audit requirements demonstrating appropriate access controls.

                Document your permission model and demonstrate you're following it.

                Permission Documentation:
                Document your permission strategy including what each role can and cannot do, how roles map to job responsibilities, approval authority for permission grants, and procedures for role assignment and revocation.

                Documentation supports training, audit, and consistent administration.

                Troubleshooting Permission Issues:
                Users sometimes report they can't access features they need. Verify their assigned role and what permissions it includes. Confirm the feature is appropriate for their role. Check if configuration restricts access beyond role. Determine if permission request is justified.

                Sometimes users request access they don't actually need or that would violate separation of duties.

                Best Practices for Permission Management:
                Assign minimum necessary permissions using least privilege principle. Review permissions regularly for appropriateness. Revoke access promptly when no longer needed. Document permission grants and justifications. Audit permission usage and anomalies. Separate duties appropriately across roles. Educate users about their permissions and boundaries. Maintain clear role definitions and documentation. Follow approval processes for elevated access. Monitor for permission abuse or policy violations.

                Effective permission management balances security, usability, and compliance. As an administrator, your careful management of roles and permissions protects SLMS and its data while enabling users to do their jobs efficiently. Treat permission management as an ongoing responsibility requiring regular attention and refinement.
                """,
                null
        );
    }

    // ================================
    // CUSTOMER SERVICE DASHBOARD DOCUMENTS
    // ================================

    private UpsertDocumentRequest createCustomerServiceDashboardDoc() {
        return new UpsertDocumentRequest(
                "Using the Customer Service Dashboard",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                The customer service dashboard in SLMS is your command center for managing delivery exceptions and providing customer support. It provides real-time visibility into exception workload, helps prioritize issues, tracks team performance, and ensures no customer issues fall through the cracks. This guide covers how to effectively use the customer service dashboard for excellence in exception management.

                Dashboard Purpose and Value:
                The customer service dashboard transforms exception data into actionable insights. It shows current workload and backlog at a glance, helps identify urgent issues requiring immediate attention, tracks your personal exceptions and overall team performance, reveals patterns that might indicate systemic problems, and supports data-driven decisions about resource allocation and prioritization.

                Regular dashboard review is essential for effective customer service operations.

                Accessing the Customer Service Dashboard:
                Log into the SLMS admin console with your customer service representative credentials. Navigate to the Customer Service Dashboard or Exceptions Dashboard section. The dashboard displays automatically showing current exception data and metrics.

                The dashboard provides both personal views showing your assigned exceptions and team views showing overall customer service performance.

                Exception Count Overview:
                The dashboard prominently displays exception counts by status. Total open exceptions shows all unresolved issues across the system. New exceptions displays recently reported issues awaiting assignment. Assigned to you shows exceptions you're personally responsible for. In progress indicates exceptions being actively worked. Resolved shows exceptions with implemented solutions awaiting closure. Backlog displays exceptions older than target age.

                These counts help you understand current workload and prioritize your work.

                Personal Work Queue:
                Your personal work queue shows exceptions assigned to you. View exceptions in ASSIGNED status awaiting your attention. See exceptions IN_PROGRESS that you're actively working. Check recently RESOLVED exceptions you've completed.

                Sort your queue by priority to ensure urgent issues get attention first, by age to identify stale exceptions, by exception type to batch similar work, or by customer to see if one customer has multiple issues.

                Team workload visibility:
                Beyond your personal queue, see team-wide exception workload. View all team members and their assigned exception counts. Identify who is overloaded vs. who has capacity. See unassigned exceptions awaiting assignment. Track team backlog and aging.

                Team visibility supports workload balancing and collaboration.

                Priority and Severity Indicators:
                The dashboard highlights high-priority and high-severity exceptions. Visual indicators like color coding show critical exceptions. Separate counts for high vs. medium vs. low priority. Filtering by priority to focus on urgent issues. Alerts when critical exceptions are aging.

                Priority indicators ensure the most important issues get immediate attention.

                Exception Type Breakdown:
                View exceptions categorized by type. Counts for damaged packages, incorrect items, non-receipt, wrong delivery location, late delivery, and other categories. Charts showing relative frequency of each type. Trends over time showing increasing or decreasing exception types.

                Type breakdown helps identify common issues and systemic problems.

                Aging and Response Time Metrics:
                Track how long exceptions have been open. Average age of open exceptions. Exceptions grouped by age brackets like less than 1 day, 1-3 days, 3-7 days, over 7 days. Average time to first response showing how quickly new exceptions get acknowledged. Average time to resolution measuring how long issues take to resolve.

                Aging metrics help ensure timely response and resolution.

                Resolution Rate Metrics:
                Monitor how effectively exceptions are being resolved. Total exceptions resolved today, this week, this month. Resolution rate as percentage of opened exceptions that are closed. Average resolution time trending over time. Comparison of resolution times by exception type.

                Resolution metrics measure customer service efficiency and effectiveness.

                Customer Satisfaction Indicators:
                If SLMS tracks customer satisfaction, the dashboard may display related metrics. Customer feedback scores after exception resolution. Positive vs. negative customer responses. Trends in satisfaction over time. Satisfaction broken down by exception type or representative.

                Satisfaction metrics provide insight into customer experience quality.

                Filter and Search Capabilities:
                Dashboards typically offer powerful filtering. Filter by exception status to see specific workflow stages. Filter by priority or severity to focus on important issues. Filter by exception type to analyze specific problem categories. Filter by assigned representative to see individual workload. Filter by date range to analyze specific time periods.

                Filtering enables targeted analysis and action.

                Dashboard Refresh and Real-Time Data:
                Dashboards often update in real-time or near-real-time. Counts update as exceptions are reported, assigned, or resolved. Your personal queue updates immediately when exceptions are assigned to you. Team metrics refresh to reflect current state.

                Real-time data ensures you're always working with current information.

                Alerts and Notifications:
                Configure dashboard alerts for important conditions. Alert when new high-priority exceptions are assigned to you. Notify when exception counts exceed thresholds. Remind about exceptions approaching service level targets. Alert on unusual spikes in particular exception types.

                Alerts enable proactive response before issues become critical.

                Quick Actions from Dashboard:
                Many dashboards allow quick actions without deep navigation. Click exception to go directly to detail page. Assign exceptions to yourself or team members. Update exception status. Send customer communication. Access customer or order details.

                Quick actions improve efficiency by reducing clicks.

                Dashboard Customization:
                Some dashboards allow personalization. Choose which widgets or metrics to display. Arrange dashboard layout to your preference. Set default filters for your typical workflow. Save custom views for different purposes.

                Customization helps you focus on information most relevant to your work.

                Mobile Access:
                If you work remotely or need mobile access, check if the customer service dashboard is mobile-accessible. Responsive design adapting to phone or tablet screens. Mobile app with dashboard access. Mobile notifications for critical exceptions.

                Mobile access provides flexibility for remote work.

                Using Dashboard for Daily Planning:
                Start each work session by reviewing the dashboard. Check your assigned exception count and prioritize your work. Identify any critical or high-priority issues needing immediate attention. Review team backlog to see if you should take additional exceptions. Check aging metrics to find exceptions that have been open too long.

                Dashboard review sets priorities and organizes your work.

                Using Dashboard for Performance Monitoring:
                Managers and representatives use the dashboard to monitor performance. Track your resolution rate and compare to team averages. Monitor your average resolution time and work to improve. Check your exception backlog and keep it manageable. Review customer feedback on your resolutions.

                Performance monitoring supports continuous improvement and accountability.

                Identifying Systemic Issues:
                Use dashboard trends to spot systemic problems. Spikes in damaged package exceptions might indicate carrier handling problems. Increases in incorrect item exceptions suggest warehouse quality issues. Geographic clusters of exceptions might reveal carrier problems in specific areas. Timing patterns might show process issues at certain times.

                Pattern recognition enables proactive problem-solving beyond individual exception resolution.

                Dashboard Reporting:
                Export dashboard data for reporting. Generate reports on exception volume and trends. Analyze resolution performance by representative or team. Produce customer service KPI reports for management. Create visualizations for stakeholder communication.

                Reporting uses dashboard data to communicate performance and justify resources.

                Best Practices for Dashboard Use:
                Review dashboard at start of each work session to prioritize. Focus on high-priority and aging exceptions first. Use filters to manage information overload and focus on relevant exceptions. Take action on insights - don't just observe metrics. Communicate with team about concerning trends or bottlenecks. Monitor your performance and work to improve metrics. Report systemic patterns to management for operational fixes. Keep dashboard data confidential - respect customer privacy.

                The customer service dashboard is your most important tool for managing exception workload and delivering excellent customer support. Regular, thoughtful use of the dashboard ensures you prioritize effectively, resolve issues efficiently, and contribute to continuous improvement in logistics operations.
                """,
                null
        );
    }

    private UpsertDocumentRequest createPrioritySeverityDoc() {
        return new UpsertDocumentRequest(
                "Understanding Exception Priority and Severity",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                Delivery exceptions in SLMS use priority and severity levels to help customer service representatives prioritize work and ensure the most critical issues receive immediate attention. Understanding how to assess and use priority and severity enables effective exception management and optimal customer service. This guide explains the priority and severity systems and how to apply them.

                Why Priority and Severity Matter:
                Not all delivery exceptions are equally urgent or impactful. Priority and severity help identify which issues need immediate attention vs. which can wait, allocate resources appropriately to high-impact issues, set customer expectations about resolution timeframes, measure performance against service level agreements, and escalate critical issues to appropriate authority levels.

                Proper prioritization ensures customers with urgent needs get rapid response.

                Priority vs. Severity:
                Priority and severity are related but distinct concepts. Priority indicates urgency - how quickly the exception must be addressed based on time sensitivity, customer impact, and business importance. Severity indicates impact - how serious the problem is in terms of customer harm, financial loss, and operational disruption.

                An exception can be high priority but low severity (urgent but minor) or low priority but high severity (serious but not urgent). Both dimensions inform how you handle the exception.

                Priority Levels:
                SLMS typically defines three priority levels. HIGH priority means immediate attention required - respond within hours, not days. Common scenarios include time-sensitive shipments that are delayed or lost, high-value customers experiencing issues, shipments with upcoming deadlines like gifts or event items, and situations generating significant customer frustration or complaints.

                MEDIUM priority indicates standard attention - respond within normal service levels like 24 hours. Most exceptions fall into medium priority including standard damaged packages, typical incorrect item issues, routine late deliveries, and general customer delivery concerns.

                LOW priority means lower urgency - can be handled after higher priorities. Examples include minor cosmetic packaging damage with undamaged contents, customer inquiries that aren't complaints, and issues that have already been partially resolved.

                Priority often depends on customer expectations and business context, not just the issue type.

                Severity Levels:
                SLMS typically defines three severity levels. CRITICAL severity indicates complete delivery failure or major customer impact. Examples include high-value items lost or destroyed, perishable or time-critical shipments that missed deadlines, safety or security concerns with shipments, and situations threatening customer relationships or reputation.

                MAJOR severity means significant problem requiring resolution. Common scenarios include substantial damage making items unusable, completely wrong items shipped, delivery to wrong address across town, and repeated failures for the same customer.

                MINOR severity indicates small issues with limited impact. Examples include cosmetic damage with items still usable, small discrepancies like one item of multi-item order, delivery slightly later than estimated, and easily corrected address issues.

                Severity reflects customer impact - how much harm or inconvenience the issue causes.

                Assigning Priority:
                When you receive or review an exception, assess appropriate priority. Consider shipping method - expedited shipments are higher priority than standard. Think about customer importance - VIP or high-volume customers get higher priority. Evaluate time sensitivity - items needed by specific dates are urgent. Consider customer frustration level - very upset customers need quicker response. Review any special circumstances making the issue urgent.

                You can adjust priority as you learn more about the exception.

                Assigning Severity:
                Assess severity based on impact. Consider financial value - high-dollar items lost or damaged are high severity. Think about customer harm - unusable products are worse than cosmetic issues. Evaluate operational impact - systemic problems affecting many orders are severe. Consider safety and security implications. Review whether the issue is isolated or part of a pattern.

                Severity helps determine appropriate resolution approaches and escalation needs.

                High Priority, Low Severity:
                Some exceptions are urgent but not serious. A customer who needs a package for an event tomorrow might have high priority due to time sensitivity even though the issue itself isn't severe. An anxious VIP customer might get high priority for business relationship reasons despite a minor issue.

                Respond quickly to high-priority exceptions regardless of severity.

                Low Priority, High Severity:
                Conversely, some exceptions are serious but not urgent. A high-value damaged package might be severe due to financial impact but if the customer isn't in rush and is patient about replacement, priority might be lower. A systemic issue affecting many orders might be severe but individual order priority might be standard if customers are understanding.

                Severe exceptions still need thorough resolution even if not immediate.

                Updating Priority and Severity:
                As you investigate exceptions, you may need to adjust priority or severity. Escalate priority if you discover time-sensitive aspects you didn't initially know. Increase severity if damage is worse than reported or additional problems are found. Reduce priority if customer indicates no urgency. Lower severity if the issue is less serious than initially described.

                Update priority and severity to reflect current understanding.

                Priority and Service Level Agreements:
                Organizations often have service level agreements (SLAs) tied to priority. High priority might require first response within 2 hours and resolution within 24 hours. Medium priority might require first response within 24 hours and resolution within 3 business days. Low priority might allow response within 2 business days and resolution within 5 business days.

                Know your SLAs and ensure you meet them for each priority level.

                Filtering by Priority and Severity:
                Use dashboard filters to focus on high-priority or high-severity exceptions. Filter for HIGH priority to see urgent issues needing immediate attention. Filter for CRITICAL severity to identify serious problems. Combine filters like HIGH priority AND CRITICAL severity for most important exceptions.

                Filtering ensures critical issues don't get lost in overall exception volume.

                Priority and Resource Allocation:
                Customer service managers use priority and severity to allocate resources. High-priority exceptions might be assigned to senior representatives. Critical severity might require manager involvement. High volume of high-priority exceptions might justify temporary staffing increases.

                Priority and severity inform resource decisions.

                Customer Communication About Priority:
                When communicating with customers, be mindful of priority. For high-priority exceptions, acknowledge urgency and commit to fast resolution. For medium priority, set realistic expectations about timeframes. For low priority, explain it will be handled but may take longer.

                Managing expectations prevents frustration even for lower-priority issues.

                Escalation Based on Priority and Severity:
                Escalate exceptions that exceed normal handling. High priority AND high severity typically requires escalation to supervisors or managers. Issues involving potential liability, safety, or major financial loss need escalation. Exceptions where you're unsure of appropriate resolution should be escalated.

                Don't hesitate to escalate when appropriate - that's what supervisors are for.

                Priority Conflicts:
                When you have multiple high-priority exceptions, further prioritize based on extreme urgency like shipments needed in hours vs. days, customer importance for business relationships, potential escalation risk if not handled immediately, and service level agreement deadlines.

                Even among high-priority items, some are more urgent than others.

                Avoiding Priority Inflation:
                Be honest in priority assignment. If everything is high priority, nothing is. Reserve HIGH priority for truly urgent situations. Use MEDIUM priority as the default for most exceptions. Use LOW priority for genuinely less urgent issues.

                Appropriate priority assignment helps the system work effectively.

                Priority and Performance Measurement:
                Customer service performance is often measured differently by priority. High-priority resolution time is tracked more carefully. SLA compliance is critical for high-priority exceptions. Meeting high-priority targets demonstrates service quality.

                Your ability to effectively handle high-priority exceptions reflects your customer service excellence.

                Best Practices for Priority and Severity:
                Assess priority and severity based on objective criteria, not just subjective impressions. Be consistent in how you apply priority and severity levels. Update priority and severity as new information emerges. Use priority to organize your daily work - tackle high priority first. Escalate when priority or severity exceeds your authority. Communicate expectations appropriate to priority level. Meet SLAs especially for high-priority exceptions. Balance urgency with thoroughness - high priority doesn't mean rushing to wrong solutions.

                Effective use of priority and severity ensures customer service resources are applied where they're most needed, urgent issues get rapid attention, and no critical exceptions fall through the cracks. Master priority and severity assessment to excel at exception management.
                """,
                null
        );
    }

    private UpsertDocumentRequest createExceptionFilteringDoc() {
        return new UpsertDocumentRequest(
                "Filtering and Searching Exceptions Effectively",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                With potentially hundreds of delivery exceptions in the system, finding the right ones to work on requires effective filtering and searching. SLMS provides powerful exception filtering capabilities that help customer service representatives focus on relevant exceptions, prioritize work, identify patterns, and ensure efficient exception management. This guide covers how to use filtering and search features to maximum advantage.

                Why Filtering Matters:
                Without filtering, you'd face an overwhelming list of all exceptions in the system. Filtering helps focus on exceptions you're responsible for, identify urgent issues needing immediate attention, group similar exceptions for efficient batch processing, find specific exceptions when customers call, and analyze patterns by type, customer, or other criteria.

                Mastering filtering capabilities dramatically improves exception management efficiency.

                Accessing Exception Filtering:
                Navigate to the Exception Management section of the admin console. You'll see an exception list with filtering controls typically at the top or side. Filters might be dropdowns, checkboxes, text fields, or date pickers. Apply filters to narrow the exception list to your criteria.

                Most systems remember your filter preferences across sessions for convenience.

                Filter by Status:
                Status filtering shows exceptions at specific workflow stages. Filter for OPEN to see newly reported exceptions awaiting assignment. Select ASSIGNED to view exceptions allocated to representatives but not yet being worked. Choose IN_PROGRESS to see exceptions being actively investigated and resolved. Pick RESOLVED to review exceptions with implemented solutions. Select CLOSED to view completed exceptions.

                Filtering by status helps you focus on exceptions at the stage you're working.

                Filter by Assignment:
                Assignment filtering helps you see your personal workload or team distribution. Filter for "Assigned to Me" to see only your exceptions. View "Unassigned" to see exceptions awaiting assignment you might take. Filter by specific team members to see their workload. Select "All" to see exceptions regardless of assignment.

                Assignment filtering is one of the most commonly used filters for daily work.

                Filter by Priority:
                Priority filtering helps identify urgent issues. Filter for HIGH priority to see urgent exceptions needing immediate attention. Select MEDIUM for standard exceptions. Choose LOW for less urgent issues. Select multiple priorities to combine them.

                Start each work session by filtering for HIGH priority to ensure urgent issues get immediate attention.

                Filter by Severity:
                Severity filtering identifies serious problems. Filter for CRITICAL severity to see major issues. Select MAJOR for significant problems. Choose MINOR for small issues. Combine severity with priority for nuanced prioritization.

                Critical severity exceptions often need special handling or escalation.

                Filter by Exception Type:
                Type filtering groups similar exceptions. Filter for "Damaged Package" to see all damage claims. Select "Incorrect Items" for picking errors. Choose "Non-Receipt" for delivery issues. Pick "Wrong Location" for address problems.

                Type filtering enables batch processing of similar exceptions with similar resolution approaches.

                Filter by Date Range:
                Date filtering focuses on exceptions from specific time periods. Filter by creation date to see recently reported exceptions or older backlog. Select resolution date to analyze recently resolved exceptions. Choose custom date ranges for specific analysis periods.

                Date filtering helps identify aging exceptions that need attention.

                Filter by Customer:
                Customer filtering shows all exceptions for specific customers. Search for customer by name or email. View all exceptions from that customer regardless of status. Identify customers with multiple concurrent issues. Analyze patterns in a customer's exception history.

                Customer filtering is essential when customers call asking about their issues.

                Filter by Warehouse or Location:
                If you support multiple warehouses, filter by origin location. Select specific warehouses to see exceptions from those facilities. Identify if particular warehouses have more exceptions. Analyze geographic patterns in exceptions.

                Location filtering helps identify systemic issues at specific facilities or with specific carriers in certain regions.

                Combining Multiple Filters:
                The real power of filtering comes from combining criteria. Filter for HIGH priority AND ASSIGNED to me to see your urgent personal workload. Select DAMAGED package AND CRITICAL severity to find serious damage claims. Choose IN_PROGRESS AND older than 3 days to find stalling exceptions.

                Experiment with filter combinations to find useful views for different purposes.

                Saving Filter Presets:
                If SLMS supports saved filters, create presets for common views. Save "My Urgent Work" as HIGH priority + ASSIGNED to me. Create "Aging Backlog" as IN_PROGRESS + older than 5 days. Build "New Damage Claims" as DAMAGED + OPEN or ASSIGNED.

                Saved presets enable one-click access to useful exception views.

                Text Search:
                Beyond structured filters, text search finds exceptions containing specific keywords. Search by tracking number to find a specific order's exception. Search customer name or email to find their exceptions. Search for keywords in exception descriptions like "broken" or "missing." Search exception reference numbers.

                Text search provides free-form flexibility when you know what you're looking for.

                Sorting Exception Lists:
                After filtering, sort the resulting list for optimal workflow. Sort by priority to see most urgent first. Sort by age to identify oldest exceptions. Sort by customer to batch exceptions for same customer. Sort by exception type to group similar issues.

                Sorting complements filtering to organize your work queue.

                Clearing Filters:
                Sometimes you need to reset and see all exceptions. Most interfaces provide "Clear All Filters" or similar functions. Clearing shows the complete exception list. Be careful - clearing filters on a high-volume system might display thousands of exceptions.

                Clear filters when you need broad visibility or to start fresh with new filter combinations.

                Filter Performance:
                On systems with large exception volumes, filtering helps performance. Unfiltered lists of thousands of exceptions load slowly. Filtering reduces the dataset for faster display. Use filters to keep displayed lists manageable.

                Filtering improves both your productivity and system performance.

                Mobile Filtering:
                If you access exception management on mobile devices, filtering is especially important. Mobile screens have limited space for displaying exceptions. Filtering to small relevant sets is essential for mobile usability. Mobile apps may offer simplified filter interfaces for touch screens.

                Master mobile filtering for effective remote exception management.

                Using Filters for Analysis:
                Beyond daily work, use filters for analysis and reporting. Filter by exception type and date range to analyze trends over time. Filter by warehouse to compare exception rates across facilities. Filter by severity to understand impact distribution. Filter by resolution status to calculate resolution rates.

                Analytical filtering provides insights for operational improvement.

                Filter Troubleshooting:
                If filters aren't showing expected results, troubleshoot by checking if filter criteria are too restrictive - loosen them to see more results. Verify you're filtering the right field - status vs. priority vs. severity. Check date ranges aren't excluding recent exceptions. Clear filters and reapply one at a time to identify problematic filters.

                Sometimes filter combinations that seem logical don't return results because no exceptions match all criteria.

                Sharing Filter Views:
                If working with team members, share useful filter combinations. Tell colleagues about helpful filter presets. Document standard filter views for common tasks. Train new team members on effective filtering approaches.

                Shared filter knowledge improves whole team efficiency.

                Default Filter Views:
                Configure your default view for exception management. Set filters to show your assigned exceptions by default. Sort by priority so urgent issues appear first. Customize the default to match your typical workflow.

                Good defaults mean you start each session looking at the right exceptions.

                Best Practices for Exception Filtering:
                Start each work session with HIGH priority filter to ensure urgent work gets attention. Use "Assigned to Me" frequently to focus on your personal responsibility. Combine filters to narrow to manageable working sets - not too broad, not too narrow. Save filter presets for views you use repeatedly. Clear filters periodically to ensure you're not missing exceptions outside your normal view. Use text search when you know specific keywords or identifiers. Filter by exception type to batch process similar issues efficiently. Leverage date filters to identify aging exceptions needing attention. Experiment with filter combinations to discover useful views. Share effective filter approaches with your team.

                Effective filtering and searching transform overwhelming exception volumes into manageable, organized worklists. Master these capabilities to work efficiently, prioritize effectively, and ensure no customer issues fall through the cracks. Filtering is a fundamental skill for exception management excellence.
                """,
                null
        );
    }

    // ================================
    // NOTIFICATIONS & SUPPORT DOCUMENTS
    // ================================

    private UpsertDocumentRequest createNotificationPreferencesDoc() {
        return new UpsertDocumentRequest(
                "Managing Your Notification Preferences",
                Set.of("CUSTOMER"),
                Set.of("customer-portal"),
                Set.of(),
                "en-US",
                """
                SLMS sends automatic notifications to keep you informed about your orders. You can customize when you receive notifications, how you receive them, and what information they include. Managing your notification preferences ensures you stay informed without being overwhelmed by messages. This guide explains how notification preferences work and how to configure them.

                Why Notifications Matter:
                Notifications provide real-time updates about your orders without requiring you to constantly check tracking pages. They inform you when orders ship, are in transit, are out for delivery, and are delivered. They alert you to exceptions or issues requiring your attention. They provide tracking updates at key milestones.

                Well-configured notifications keep you informed efficiently.

                Types of Notifications:
                SLMS typically sends notifications for several order events. Order confirmation when you place an order or when warehouse staff creates your order. Shipping confirmation when your order leaves the warehouse and enters carrier network. Delivery updates when your package is out for delivery or approaching. Delivery confirmation when your package is successfully delivered. Exception notifications when issues are reported or resolved.

                Different notification types serve different purposes throughout the order lifecycle.

                Notification Channels:
                You can typically receive notifications through multiple channels. Email notifications go to your registered email address. SMS text messages provide mobile notifications to your phone number. Mobile app push notifications if SLMS has a mobile app. In some cases, multiple channels can be enabled for important notifications.

                Choose channels that match how you prefer to receive information.

                Accessing Notification Preferences:
                Log into your customer portal and navigate to Account Settings, Profile Settings, or Notification Preferences. You'll see options to configure notification types, channels, and preferences. Make your selections and save changes.

                Preferences apply to future notifications - they don't affect notifications already sent.

                Configuring Email Notifications:
                Email is the most common notification channel. Enable or disable email notifications entirely. Select which events trigger emails such as order confirmation, shipping confirmation, delivery confirmation, or exception alerts. Verify your email address is correct and current. Check spam folders if you're not receiving expected emails.

                Email notifications provide detailed information and permanent records you can reference later.

                Configuring SMS Notifications:
                SMS provides immediate mobile notifications. Enable or disable SMS text messages. Provide and verify your mobile phone number. Select which events trigger SMS such as shipping and delivery confirmation. Be aware that SMS is usually brief - detailed information requires checking email or portal.

                SMS is ideal for time-sensitive notifications like out for delivery and delivered.

                Notification Timing:
                Some systems allow timing preferences. Receive notifications immediately when events occur. Aggregate notifications into daily or weekly summaries. Set quiet hours when you don't want to receive notifications.

                Timing preferences help balance staying informed with avoiding notification overload.

                Language Preferences:
                If SLMS supports multiple languages, select your preferred notification language. Choose the language for email content. Set language for SMS messages. Ensure notifications are in a language you understand.

                Language preferences improve notification usefulness.

                Notification Frequency:
                Manage how often you receive certain notifications. Receive every tracking update vs. only major milestones. Get notifications for every order vs. only specific orders. Enable summaries instead of individual messages for high-volume customers.

                Frequency controls prevent notification fatigue.

                Disabling Notifications:
                If you prefer not to receive notifications, you can disable them. Turn off all notifications if you'll track orders manually. Disable specific notification types while keeping others. Temporarily pause notifications if you'll be unavailable.

                Remember that disabling notifications means you must check tracking manually to stay informed.

                Re-enabling Notifications:
                If you've disabled notifications and want to re-enable them, access notification preferences and toggle notifications back on. Select which types to receive. Save changes to activate notifications.

                Notifications will resume for future order events but won't be sent retroactively for events that occurred while they were disabled.

                Order-Specific Notifications:
                Some systems allow notification preferences per order. Enable or disable notifications for individual orders. Set different preferences for high-priority vs. routine orders. Configure who receives notifications if ordering on behalf of others.

                Order-specific preferences provide granular control.

                Exception and Issue Notifications:
                Exception notifications inform you about delivery problems and resolutions. Receive notification when you report an exception. Get updates when customer service works on your exception. Be notified when exceptions are resolved.

                Exception notifications help you track issue resolution without repeatedly checking.

                Delivery Confirmation Notifications:
                Delivery confirmation is one of the most important notification types. Be notified immediately when your package is delivered. Receive delivery timestamp and location information. Get confirmation even if you were home when delivered.

                Delivery confirmation provides peace of mind and helps you know when to check for your package.

                Managing Notification Overload:
                If you receive too many notifications, adjust preferences. Disable notifications for less important events. Choose summary digests instead of individual messages. Limit notifications to most critical updates only. Consider disabling SMS while keeping email for less urgency.

                Finding the right notification balance ensures you stay informed without being overwhelmed.

                Updating Contact Information:
                Notification preferences depend on accurate contact information. Keep your email address current in your account profile. Update your phone number if it changes. Verify contact information periodically to ensure you're reachable.

                Outdated contact information means you won't receive notifications even if enabled.

                Testing Notifications:
                After changing notification preferences, verify they work. Place a test order if possible to see notifications. Check that emails arrive and aren't going to spam. Verify SMS messages are received at your phone number. Ensure notifications contain expected information.

                Testing confirms your preferences are working correctly.

                Notification Content:
                Notifications typically include key information. Order tracking number for reference. Current order status. Event that triggered notification such as shipped or delivered. Link to track the order in the customer portal. Contact information for customer service if you have questions.

                Notification content provides essential information at a glance with option to get more details.

                Privacy and Notifications:
                Be aware that notifications contain order information. Email and SMS are not fully secure channels. Avoid having notifications sent to shared email accounts or phones. Be cautious about notification privacy if others have access to your email or phone.

                Configure notifications to balance convenience with privacy needs.

                Notification Troubleshooting:
                If you're not receiving expected notifications, check that notifications are enabled in your preferences, verify your email address and phone number are correct, check spam or junk folders for email notifications, ensure your phone can receive SMS from unknown numbers, and contact customer service if issues persist.

                Most notification problems are due to misconfigured preferences or incorrect contact information.

                Unsubscribe Links:
                Marketing regulations often require unsubscribe links in emails. Be careful not to unsubscribe from transactional order notifications when you just mean to unsubscribe from marketing emails. Unsubscribing from order notifications means you won't receive important order updates.

                Read unsubscribe options carefully to ensure you're only disabling what you intend.

                Best Practices for Notification Management:
                Enable notifications for key events like shipping and delivery confirmation. Provide accurate email and phone number. Choose notification channels that match your preferences - email for detail, SMS for immediacy. Review notifications when received to stay informed about your orders. Update notification preferences if they're not meeting your needs. Don't disable all notifications unless you're committed to manually checking tracking. Use notifications as triggers to check the customer portal for full details. Manage notification volume to avoid overload while staying informed.

                Well-managed notification preferences keep you informed about your orders efficiently, help you know when to expect deliveries, alert you to any issues, and provide peace of mind without overwhelming you with messages. Take a few minutes to configure notifications to match your preferences.
                """,
                null
        );
    }

    private UpsertDocumentRequest createContactSupportDoc() {
        return new UpsertDocumentRequest(
                "How to Contact Customer Support",
                Set.of("CUSTOMER", "DRIVER", "WAREHOUSE_STAFF", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal", "driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                When you have questions about SLMS, need help with features, or require assistance with orders and deliveries, customer support is available to help. Understanding when and how to contact support ensures you get the assistance you need quickly and effectively. This guide covers support options, when to use them, and how to get the best help.

                When to Contact Support:
                Contact customer support when you have delivery issues not resolved through the exception system, questions about orders or tracking, problems accessing your account or using SLMS features, billing or payment questions, technical issues with the website or app, or feedback or suggestions about SLMS.

                Before contacting support, try self-service options like checking order tracking, reviewing FAQ and help documentation, verifying your account information, or using the chatbot for common questions.

                Self-service often resolves issues faster than waiting for support response.

                Support Channels:
                SLMS typically offers multiple support channels. The chatbot assistant provides 24/7 automated help for common questions. Email support for non-urgent inquiries and detailed questions. Phone support for urgent issues or complex situations requiring conversation. Live chat for real-time text conversation with support representatives. Help documentation and FAQs for self-service answers.

                Choose the channel that matches your needs and urgency.

                Using the Chatbot Assistant:
                The SLMS chatbot is available 24/7 in the customer portal and on the public site. Click the chat icon or Help button to open the chatbot. Ask your question in natural language. The chatbot searches its knowledge base and provides relevant answers. For simple questions about features, tracking, or processes, the chatbot often provides instant answers.

                If the chatbot can't help, it can escalate you to human support.

                When to Use the Chatbot:
                The chatbot excels at answering questions like how do I track my order, what do order statuses mean, how do I report a delivery issue, what are your hours or policies, and where do I find specific features in SLMS.

                The chatbot has comprehensive knowledge about SLMS features and processes.

                When to Escalate to Human Support:
                Escalate to human support when the chatbot doesn't understand your question, you need to discuss specific details of your order or situation, the issue requires judgment or discretion, you're frustrated and want to speak with a person, or the problem is technical or complex.

                Human support handles situations requiring empathy, judgment, and detailed investigation.

                Contacting Email Support:
                For non-urgent issues, email support is convenient. Find the support email address in the Contact Us section. Send an email describing your issue including order tracking numbers, account email, detailed description of the problem, what you've tried to resolve it, and screenshots if relevant.

                Email support typically responds within 24-48 hours.

                Contacting Phone Support:
                For urgent issues, phone support provides immediate assistance. Find the support phone number in the Contact Us section. Have relevant information ready including your account email or customer ID, order tracking numbers, and description of your issue. Be prepared for possible wait times during busy periods.

                Phone support is best for urgent issues or when you need to discuss complex situations.

                Using Live Chat Support:
                Live chat combines the immediacy of phone with the documentation of email. Look for the Live Chat option in the customer portal or website. Click to start a chat session with a support representative. Describe your issue in the chat. The representative will respond in real-time, ask questions, and help resolve your issue.

                Live chat is great when you want immediate help but prefer text communication.

                Providing Information to Support:
                Help support help you by providing relevant information. Always include your account email address or customer ID, order tracking numbers for order-related issues, detailed description of the problem and when it started, what you've tried to resolve it, screenshots or photos if they help illustrate the issue, and your preferred resolution if you have one.

                Complete information enables support to help you faster.

                Support Hours:
                Check support channel availability. The chatbot is available 24/7. Email support may be monitored during business hours with response within 24-48 hours. Phone support may be available during specific hours. Live chat may be available during extended hours.

                Know support hours to set expectations for response timing.

                Support Response Times:
                Different channels have different response times. The chatbot responds instantly. Live chat connects you with a representative within minutes typically. Phone support response depends on call volume and wait times. Email support typically responds within 24-48 hours.

                For urgent issues, use phone or live chat rather than email.

                Support for Different User Types:
                Support varies by user role. Customers contact support for order tracking, delivery issues, and account questions. Drivers contact support for driver app technical issues and delivery confirmation problems. Warehouse staff contact support for admin console issues and system questions. Customer service representatives may have internal support channels for escalation.

                Identify yourself and your role when contacting support.

                Language Support:
                Check what languages support offers. Primary support may be in English. Additional language support may be available. Chatbot may support multiple languages. Specify your preferred language when contacting support.

                Language support ensures clear communication.

                Support for Technical Issues:
                For technical problems, provide detailed information. Describe what you were trying to do when the problem occurred. Note any error messages you received. Provide browser type and version if it's a website issue. Include device type and app version for mobile issues. Share screenshots of error messages or problems.

                Technical details help support diagnose and resolve tech issues.

                Following Up on Support Requests:
                If you don't receive timely response, follow up. For email, reply to your original message after reasonable time. For phone, call back if you don't receive promised callback. Reference your previous ticket number or case number if provided. Be polite but persistent if your issue requires follow-up.

                Appropriate follow-up ensures your issue doesn't get lost.

                Escalating Unresolved Issues:
                If support doesn't resolve your issue satisfactorily, escalate. Ask to speak with a supervisor or manager. Request escalation to higher-level support. Explain you've attempted resolution and need additional help.

                Escalation is appropriate when standard support can't solve your problem.

                Providing Feedback About Support:
                Share feedback about your support experience. Complete satisfaction surveys if offered. Send positive feedback when support provides excellent service. Provide constructive feedback when support could improve. Suggest improvements to support processes or resources.

                Your feedback helps improve support for everyone.

                Support for Billing Issues:
                For billing or payment questions, contact support with relevant details. Include order numbers related to charges. Describe the billing issue clearly. Provide any relevant payment information (never send full credit card numbers via email). Indicate your desired resolution.

                Billing issues may be handled by specialized support team.

                Privacy and Security When Contacting Support:
                Protect your privacy when contacting support. Never send passwords or full credit card numbers. Use official support channels, not suspicious emails or links. Verify you're on the official SLMS site when using chat or forms. Be cautious about sharing sensitive personal information.

                Official support will never ask for your password.

                Best Practices for Contacting Support:
                Try self-service options first like tracking, help docs, and chatbot. Use the appropriate channel for your issue urgency and type. Provide complete, detailed information in your initial contact. Be polite and patient with support representatives. Have relevant information ready before contacting. Follow up if you don't receive timely response. Provide feedback about your support experience. Document important support interactions for your records.

                Effective use of customer support ensures you get help when you need it while making the best use of support resources. Well-informed support requests enable faster, more effective assistance. Remember that support representatives want to help - clear communication and relevant information enable them to resolve your issues quickly.
                """,
                null
        );
    }

    // ================================
    // TROUBLESHOOTING & FAQ DOCUMENTS
    // ================================

    private UpsertDocumentRequest createCustomerFAQDoc() {
        return new UpsertDocumentRequest(
                "Frequently Asked Questions for Customers",
                Set.of("CUSTOMER", "ANONYMOUS"),
                Set.of("landing-page", "customer-portal"),
                Set.of(),
                "en-US",
                """
                This FAQ addresses common questions customers have about using SLMS, tracking orders, and resolving delivery issues. Find quick answers to common questions without needing to contact support.

                Account and Access Questions:

                How do I create an account? Navigate to the customer portal registration page, provide your email, create a password, fill in contact information, and verify your email through the confirmation link sent to you.

                Do I need an account to track orders? No, you can track orders using just the tracking number on the public tracking page. However, an account gives you access to order history, document downloads, and exception reporting.

                I forgot my password, how do I reset it? Click "Forgot Password" on the login page, enter your email address, check your email for the password reset link, and follow the link to create a new password.

                How do I update my account information? Log into the customer portal, navigate to Account Settings or Profile, update your information, and save changes.

                Order Tracking Questions:

                Where do I find my tracking number? Your tracking number appears in the order confirmation email, in your customer portal order list, and on the shipping label if you have access to the physical package.

                What do the different order statuses mean? CREATED means your order is in the system awaiting processing. READY_FOR_PICKUP means the warehouse has prepared your package. COLLECTED means the carrier picked up your package. IN_TRANSIT means your package is moving toward you. DELIVERED means your package arrived.

                Why isn't my tracking updating? Tracking may not update immediately during carrier handoffs, after hours when facilities are closed, or for packages in transit between scans. Allow 24 hours between updates before being concerned.

                How long will delivery take? Delivery time depends on shipping method and distance. Standard ground is typically 3-7 days. Expedited services are faster. Check your order details for estimated delivery date.

                Can I change my delivery address after ordering? Contact customer service immediately with the new address. Address changes are possible before packages are out for delivery but may be difficult or impossible after that.

                Delivery and Reception Questions:

                How will I know when my package is delivered? You'll receive a delivery confirmation notification via email or SMS. The order tracking will update to DELIVERED status with a delivery timestamp.

                What if I'm not home for delivery? Drivers may leave packages in safe locations depending on the shipment and your delivery instructions. For signature-required deliveries, the carrier will attempt redelivery or hold the package for pickup.

                My tracking shows delivered but I didn't receive the package, what do I do? First check everywhere the package might have been left - front door, back door, mailbox, building office, with neighbors. If you still can't find it, report a non-receipt exception through the customer portal.

                The package was delivered to the wrong address, how do I fix this? Report a wrong delivery location exception immediately through the customer portal. Customer service will investigate and coordinate retrieval or replacement.

                Damage and Issues Questions:

                My package arrived damaged, what should I do? Report a damaged package exception through the customer portal immediately. Include photos of the damage to packaging and contents. Customer service will arrange replacement or refund.

                I received the wrong items, how do I get the correct ones? Report an incorrect items exception with details about what you ordered vs. what you received. Customer service will arrange return of wrong items and shipment of correct items.

                How do I report a delivery issue? Log into the customer portal, find the order with the issue, click "Report Issue" or "Create Exception," select the problem type, provide detailed description, and submit. Customer service will investigate and resolve.

                How long does exception resolution take? Resolution time varies by issue complexity. Simple issues may be resolved in hours or days. Complex issues requiring carrier investigation may take longer. Customer service will provide estimated timeframes.

                Documents and Records Questions:

                How do I download my shipping documents? Log into the customer portal, find your order, navigate to the Documents section, and click download next to the document you need.

                Can I get proof of delivery? Yes, proof of delivery is automatically created when your package is delivered. Access it through your order details in the customer portal.

                Where can I find my order history? Log into the customer portal and navigate to Order History or Past Orders to see all your previous shipments.

                Notifications Questions:

                How do I stop receiving so many notifications? Navigate to your account Notification Preferences, disable notifications for events you don't want to be notified about, or switch from email to SMS or vice versa.

                I'm not receiving any notifications, why? Check that notifications are enabled in your preferences, verify your email address is correct, check spam folders for emails, and ensure your contact information is up to date.

                System and Features Questions:

                Is there a mobile app? SLMS customer portal is accessible through mobile web browsers. Check if a dedicated mobile app is available for your platform.

                How do I contact customer support? Use the chatbot for quick questions, send email for non-urgent issues, call phone support for urgent issues, or use live chat for real-time assistance. Contact details are in the Contact Us section.

                Is my data secure? SLMS uses industry-standard security measures including encrypted connections, secure data storage, and access controls. Never share your password with anyone.

                Can I delete my account? Contact customer support to request account deletion. Note that deletion removes your access to order history.

                This FAQ covers common questions. If you don't find your answer here, use the chatbot assistant or contact customer support for personalized help.
                """,
                null
        );
    }

    private UpsertDocumentRequest createDriverFAQDoc() {
        return new UpsertDocumentRequest(
                "Frequently Asked Questions for Drivers",
                Set.of("DRIVER", "ADMIN"),
                Set.of("driver-pwa", "admin-console"),
                Set.of(),
                "en-US",
                """
                This FAQ addresses common questions drivers have about using the SLMS driver PWA, confirming deliveries, and handling delivery issues.

                Driver App Access Questions:

                How do I access the driver PWA? Open your mobile browser, navigate to the driver PWA URL provided by your logistics manager, and bookmark or add to home screen for quick access.

                I forgot my login credentials, what should I do? Contact your warehouse manager or logistics administrator who can reset your password.

                Can I use the driver app on both iOS and Android? Yes, the PWA works on any smartphone or tablet through the web browser, supporting both iOS and Android.

                Do I need to download an app? No, the PWA works through your browser without app store downloads, but you can add it to your home screen for app-like access.

                QR Code Scanning Questions:

                How do I scan a QR code to confirm delivery? Open the driver PWA, tap the "Scan QR Code" or "Confirm Delivery" button, point your phone camera at the QR code on the package label, and hold steady while the app scans.

                The QR code won't scan, what should I do? Ensure good lighting, clean your camera lens, hold your phone steady and let the camera focus. If it still won't scan, contact your dispatcher for alternative confirmation methods.

                What if the QR code is damaged or missing? Contact your dispatcher immediately. They can provide alternative delivery confirmation procedures for that specific package.

                Do I need internet connection to scan? The app requires internet to upload confirmations, but if you scan in an area without signal, the app will queue the confirmation and upload it automatically when you regain connectivity.

                Can I scan multiple packages at once? No, scan each package individually after delivering it to ensure accurate confirmation for each delivery.

                Delivery Confirmation Questions:

                When should I scan the QR code? Scan immediately after successfully delivering the package - after handing it to the recipient or placing it in the agreed safe location.

                What happens when I scan the QR code? The scan immediately updates the order status to DELIVERED, records the timestamp, triggers customer notification, and creates proof of delivery.

                What if I forget to scan a delivery? Scan as soon as you remember. Contact your dispatcher if you've left the location and can't remember which package was for that address.

                Can I batch scan all deliveries at the end of the day? No, scan each delivery immediately after completing it for accurate timestamps and real-time customer notifications.

                Delivery Problems Questions:

                The recipient isn't home, what should I do? Follow your organization's procedures for unattended delivery or failed delivery attempts. You may leave the package in a safe location, attempt redelivery, or return it to the facility. Report the situation to your dispatcher.

                I can't find the delivery address, who do I contact? Contact your dispatcher immediately with the address details. They can verify the address, contact the customer, or provide guidance.

                The customer refuses the delivery, what should I do? Don't force delivery if customer refuses. Contact your dispatcher, note the reason for refusal, and follow their instructions about returning the package.

                Technical Issues Questions:

                The driver app isn't working, who do I contact? Contact your dispatcher or warehouse manager immediately for technical support or alternative delivery confirmation methods.

                The app is slow or freezing, what should I do? Try closing and reopening the app, ensure you have good internet connection, restart your phone if needed. Contact support if problems persist.

                Camera isn't activating for QR scanning, how do I fix it? Check that you've granted camera permissions to the browser or app, ensure no other app is using the camera, and restart your phone if needed.

                Delivery Best Practices Questions:

                Where should I leave packages for unattended delivery? Follow delivery instructions when provided, use safe secure locations out of weather and not visible from street, and document with photo if your system supports it.

                How do I handle fragile packages? Handle with care, don't stack heavy items on top, place upright if marked "This Side Up," and ensure safe placement on delivery.

                What information do I need for each delivery? You need the delivery address, recipient name, package QR code or label, and any special delivery instructions.

                Account and Security Questions:

                Should I share my login credentials? No, never share your login with anyone. Your credentials are tied to your delivery confirmations for accountability.

                What if I suspect someone is using my account? Report immediately to your manager and change your password.

                How do I log out? Use the logout option in the driver app menu, especially important if using a shared device.

                Performance and Records Questions:

                Can I see my delivery history? The driver app may show your recent deliveries. Contact your manager for complete delivery records.

                How are my deliveries tracked? Every QR code scan is logged with timestamp and your driver account ID creating an audit trail of your deliveries.

                What if I need proof I delivered a package? The system records your delivery confirmation with timestamp. Contact your manager if you need documentation about specific deliveries.

                Off-Hours and Emergency Questions:

                What if I have issues outside business hours? Contact the emergency or after-hours number provided by your organization for urgent delivery problems.

                Who do I call about route or schedule questions? Contact your dispatcher or warehouse coordinator for routing and scheduling issues.

                This FAQ covers common driver questions. For issues not addressed here, contact your dispatcher, warehouse manager, or logistics administrator for assistance.
                """,
                null
        );
    }

    private UpsertDocumentRequest createWarehouseFAQDoc() {
        return new UpsertDocumentRequest(
                "Frequently Asked Questions for Warehouse Staff",
                Set.of("WAREHOUSE_STAFF", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                This FAQ addresses common questions warehouse staff have about using SLMS for order processing, fulfillment, and warehouse operations.

                Order Processing Questions:

                How do I create a new order? Navigate to Orders section, click "Create New Order," fill in customer information, package details, shipping method, and origin warehouse, then submit.

                What information is required to create an order? You need customer name and contact information, complete delivery address, package dimensions and weight, shipping service level, and any special instructions.

                How do I update an order status? Open the order detail page, select the new status from the dropdown, add any relevant notes, and save the update.

                When should I update status to READY_FOR_PICKUP? Update when you've completely finished picking, packing, labeling, and staging the package for carrier pickup.

                When should I update status to COLLECTED? Update after the carrier has physically taken possession of the packages, verified by carrier manifest or confirmation.

                Shipping Label Questions:

                How do I generate a shipping label? From the order detail page, select "Generate Label," choose the carrier, verify address and package details, and generate. The label will be created and available for printing.

                The label won't generate, what's wrong? Check that package weight and dimensions are entered, verify the destination address is valid, ensure carrier integration is working. Contact IT support if problems persist.

                Can I regenerate a label if it prints incorrectly? Yes, you can regenerate labels. The new label replaces the old one in the system.

                Do I need to upload labels manually? No, generated labels are automatically uploaded to the order. You only manually upload documents that aren't auto-generated.

                Document Management Questions:

                What documents do I need to upload? Upload commercial invoices for international shipments, packing lists if required, photos for high-value items, and any special documentation not auto-generated.

                How do I upload a document? Open order detail, navigate to Documents section, click Upload, select the file and document type, and save.

                What file formats are supported? SLMS typically accepts PDF, PNG, and JPEG formats. Check system documentation for specific requirements.

                Where do uploaded documents go? Documents are attached to the order record and accessible by customers, customer service, and authorized warehouse staff.

                Inventory and Picking Questions:

                What if an ordered item is out of stock? Coordinate with your supervisor and customer service to inform the customer and determine whether to substitute, wait for restock, or cancel the order.

                How do I record inventory picked for orders? If using integrated inventory systems, scanning items during picking may automatically update inventory. Otherwise, follow your manual inventory procedures.

                What if I pick the wrong items by mistake? Put the wrong items back, pick the correct items, and be extra careful to verify items before packing.

                Packing and Quality Questions:

                How should I pack items to prevent damage? Use appropriately sized boxes, adequate cushioning material, secure sealing, and extra protection for fragile items.

                What if the package is too large or heavy? Use appropriate packaging for size and weight, potentially split into multiple packages, and ensure you can safely handle it.

                Should I double-check what I packed? Yes, verify items and quantities match the order before sealing and labeling. Quality control reduces exception rates.

                Carrier Pickup Questions:

                Where should I stage packages for pickup? Place packages in your designated pickup area organized by carrier if you use multiple carriers.

                What if carriers don't pick up some packages? Leave those packages in READY_FOR_PICKUP status, investigate why they weren't picked up, and coordinate with carriers or your supervisor.

                How do I know which packages were picked up? Get carrier manifest or confirmation, and update those specific orders to COLLECTED status.

                System Access and Permissions Questions:

                I can't access certain features, why? Your access is based on your user role and assigned warehouse. Contact your administrator if you need additional permissions.

                I forgot my password, how do I reset it? Contact your warehouse manager or system administrator to reset your password.

                Can I process orders from other warehouses? Only if you're specifically granted cross-warehouse access. Typically, you should only process orders assigned to your warehouse.

                Returns and Exceptions Questions:

                How do I process a return? Receive the package with return authorization number, inspect items and condition, update SLMS to confirm receipt, process items per instructions such as restock or discard, and notify customer service.

                What if a return doesn't match the authorization? Contact customer service immediately to investigate the discrepancy before processing.

                Should I update orders related to exceptions? Only update statuses based on actual physical progress. Customer service handles exception workflow separately.

                Multiple Warehouses Questions:

                How do I know which warehouse an order belongs to? Check the order's assigned warehouse field. Only process orders assigned to your warehouse.

                Can I reassign orders to different warehouses? Contact your supervisor or administrator who can reassign orders if needed.

                Performance and Reporting Questions:

                How is warehouse performance measured? Common metrics include processing time, accuracy rate, damage rate, throughput, and on-time staging for pickup.

                Where can I see performance metrics? Warehouse managers and administrators can access performance dashboards and reports in the admin console.

                Troubleshooting Questions:

                The system is slow or not responding, what should I do? Check your internet connection, try refreshing the page, clear browser cache if needed, and contact IT support if problems persist.

                I made a mistake on an order, how do I fix it? If the order hasn't shipped, coordinate with your supervisor to correct it. If it has shipped, inform customer service who can handle as an exception if needed.

                Technical Support Questions:

                Who do I contact for system issues? Contact your warehouse manager, IT support, or system administrator depending on your organization's support structure.

                Who handles customer questions about orders? Customer service handles customer inquiries. Don't communicate directly with customers unless specifically instructed.

                This FAQ covers common warehouse questions. For issues not addressed here, contact your warehouse manager or system administrator for guidance.
                """,
                null
        );
    }

    private UpsertDocumentRequest createCustomerServiceFAQDoc() {
        return new UpsertDocumentRequest(
                "Frequently Asked Questions for Customer Service Representatives",
                Set.of("CUSTOMER_SERVICE_REP", "ADMIN"),
                Set.of("admin-console"),
                Set.of(),
                "en-US",
                """
                This FAQ addresses common questions customer service representatives have about managing exceptions, coordinating resolutions, and using SLMS customer service features.

                Exception Management Questions:

                How do I assign an exception to myself? Open the exception detail page, click "Assign to Me" or select yourself from the assignment dropdown, and save.

                Can I reassign exceptions to other representatives? Yes, if you have appropriate permissions. Open the exception, change the assigned representative, add notes explaining why, and save.

                What's the difference between priority and severity? Priority indicates urgency - how quickly it must be addressed. Severity indicates impact - how serious the problem is. Both inform how you handle the exception.

                How do I update exception status? Open the exception detail page, select the new status from the dropdown, add notes about why status is changing, and save.

                When should I mark an exception as RESOLVED vs. CLOSED? Mark RESOLVED when the solution is implemented (replacement shipped, refund processed, etc.). Mark CLOSED when everything is complete and customer is satisfied.

                Investigation and Resolution Questions:

                How do I investigate a non-receipt exception? Review driver delivery confirmation via QR scan, check delivery timestamp and location details, verify the shipping address was correct, contact carrier for additional information, and check with customer about possible delivery locations they haven't checked.

                How do I coordinate a replacement shipment? Work with warehouse to create a new order for replacement items, potentially expedite shipping, update the exception with new tracking number, and communicate timeline to customer.

                How do I initiate a return? Create return authorization in the system, generate prepaid return shipping label, send label and instructions to customer via email, coordinate with warehouse to expect the return, and track return shipment.

                How long should exception resolution take? It varies by complexity. Simple issues might be resolved in hours or days. Complex issues requiring carrier investigation may take longer. Set realistic expectations with customers.

                Communication Questions:

                How do I communicate with customers about their exceptions? Use SLMS messaging tools to send updates, explain what you're doing, set expectations, and request additional information if needed.

                What should I say in my initial response? Acknowledge their issue, introduce yourself, indicate you're investigating, set expectations for resolution timeframe, and thank them for their patience.

                How often should I update customers? Provide updates when you have new information, at least every 2-3 days for longer investigations, and whenever you're taking action like coordinating with carriers.

                Carrier Coordination Questions:

                How do I contact carriers about exceptions? Use carrier customer service numbers, email support, or online portals. Have tracking number and details ready. Document all interactions in exception notes.

                What if the carrier won't help me? Escalate within the carrier's support structure, involve your supervisor for manager-to-manager escalation, and document uncooperative carrier behavior.

                How do I file a damage claim with carriers? Follow the carrier's claim process, provide required documentation like photos and values, obtain claim numbers, track claim status, and update the exception with claim information.

                Warehouse Coordination Questions:

                When do I need to involve warehouse staff? For incorrect item exceptions, when requesting document uploads, to coordinate return receipt and processing, and to report recurring quality issues.

                How do I communicate with warehouse? Use established channels like email to warehouse managers, internal messaging systems, or ticketing systems.

                What if warehouse isn't responsive? Escalate to warehouse management or your supervisor to ensure coordination happens.

                Escalation Questions:

                When should I escalate an exception? When the issue exceeds your authority level, involves high-dollar amounts, contains complex situations you're unsure how to handle, or when customers threaten legal action or major complaints.

                Who do I escalate to? Escalate to your supervisor or team lead with summary of issue, investigation findings, and your recommendation.

                Workload and Performance Questions:

                How many exceptions should I handle at once? This varies by organization and exception complexity. Monitor your workload and communicate with your supervisor if you're overwhelmed.

                How is my performance measured? Common metrics include response time, resolution time, exception resolution rate, customer satisfaction, and quality of documentation.

                What if I can't meet service level targets? Communicate with your supervisor about workload, complexity, or blockers preventing you from meeting targets.

                System and Tools Questions:

                Where do I find customer order history? Access customer accounts through customer search, then view their order history to see past orders and exceptions.

                How do I filter exceptions to find specific ones? Use status filters, assignment filters, priority/severity filters, exception type filters, and date range filters. Combine filters for targeted searches.

                Can I export exception data? Check with your administrator about export capabilities for reporting and analysis.

                Refunds and Financial Questions:

                How do I process refunds? Follow your organization's refund process which may be through SLMS or external systems. Document refund details in the exception.

                Who approves high-value refunds or replacements? Check your authority limits. High-value resolutions may require supervisor approval.

                Documentation and Compliance Questions:

                How detailed should my exception notes be? Document everything - investigation steps, who you contacted and when, findings, actions taken, and resolution details. Good notes support audits and future reference.

                Are exception records kept permanently? Exception records are typically retained long-term for audit, compliance, and historical reference.

                Best Practices Questions:

                What's the key to good exception management? Respond promptly, investigate thoroughly, communicate clearly with customers, coordinate effectively with carriers and warehouse, document everything, and follow through to complete resolution.

                How do I avoid exception burnout? Prioritize effectively, take breaks, collaborate with teammates, celebrate resolutions, and remember you're helping customers.

                How do I handle difficult or angry customers? Stay calm and empathetic, acknowledge their frustration, focus on solutions not blame, set realistic expectations, and escalate if the situation becomes abusive.

                Technical Support Questions:

                What if SLMS isn't working properly? Contact IT support or your system administrator, document what's not working, and work around issues while they're being resolved.

                Who provides training on new features? Your supervisor or organization's training team should provide training on system updates and new features.

                This FAQ covers common customer service questions. For issues not addressed here, consult your supervisor, team lead, or system administrator for guidance. Remember that your team members are also valuable resources for learning and problem-solving.
                """,
                null
        );
    }
}
