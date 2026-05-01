Based on the `instructions.md` document, to get full marks, we need to implement the remaining actors and physical hardware interfaces as UI simulations.

Here is the breakdown of the **6 UIs/Portals** we need to build, along with what each must contain:

### 1. Entrance Panel
*Role: Simulates the physical machine at the parking gates.*
*   **Capacity Status:** A large indicator showing if the lot is "Full" or has "Space Available." If full, it must explicitly block entry.
*   **Vehicle Type Selector:** Buttons or a dropdown to select the arriving vehicle type (Car, Truck, Motorcycle, Van, Electric).
*   **Action:** A "Get Ticket" button.
*   **Output:** The system must assign a parking spot and display a simulated printed ticket (Ticket Number, Entry Time, Assigned Spot).

### 2. Automated Exit Panel
*Role: Simulates the boom-gate payment machine where cars leave.*
*   **Ticket Scanner:** An input field to enter the ticket number.
*   **Fee Display:** Calculates and shows the total fee based on the time parked and the dynamic `ParkingRate`.
*   **Payment Gateway:** A credit card input form.
*   **Output:** Success/Error messages, a simulated receipt, and a "Gate Opened" visual confirmation.

### 3. Customer Info Portal (Kiosk)
*Role: A machine located on each floor where customers can pay before returning to their car.*
*   **Ticket Scanner:** Input to scan their ticket.
*   **Payment Gateway:** Credit card payment form.
*   **Logic:** Once paid here, the system must mark the ticket as "Paid". When the customer drives to the `Exit Panel`, the exit panel must recognize it is already paid and just open the gate.

### 4. Parking Attendant Portal
*Role: A secure dashboard for logged-in staff members.*
*   **Authentication:** Requires the attendant to log in.
*   **Profile Management:** Ability to view and update their own account details.
*   **Manual Override/Assistance:**
    *   Generate tickets manually if the entrance panel fails.
    *   **Cash Payments:** Crucially, attendants are the *only* ones who can process cash payments and calculate change. The UI needs a cash register simulation.

### 5. Parking Display Board
*Role: Large TV screens placed on each floor.*
*   **Real-time Availability:** A live-updating dashboard showing exactly how many spots are free for *each specific type* (Compact, Large, Handicapped, Motorcycle, Electric) on that specific floor.

### 6. Electric Panel (For EV Spots)
*Role: Specialized kiosks at electric parking spots.*
*   **Charging Controls:** Start/Stop charging.
*   **Payment:** Pay for the electricity consumed (can be separate or added to the parking ticket).