/* 
 Functions Overview:
   - `selectRole(role)`: Handles role-based redirection after a user selects a specific role.
   - `renderContent()`: Manages the content rendering process based on the user’s role.

 `selectRole(role)`:
   - The function handles role-based redirection after a user logs in or selects a role.
   - `role`: Determines where the user will be redirected after role selection. This can be `admin`, `patient`, `doctor`, or `loggedPatient`.
   
   Steps:
   - First, retrieve the `token` from `localStorage` for role validation and navigation.
   - If the `role` is "admin", the user is redirected to the admin dashboard using their token.
   - If the `role` is "patient", the user is sent to the patient dashboard.
   - If the `role` is "doctor", the user is redirected to the doctor dashboard, using the token if available.
   - If the `role` is "loggedPatient", they are redirected to the logged-in patient dashboard.

 `renderContent()`:
   - This function renders content based on the user’s role.
   - First, retrieve the user's role using `getRole()`.
   - If no role is found, redirect the user to the role selection page (home page).
   - Role-specific content would be handled after determining the role. 
   
 Error Handling:
   - Both functions ensure proper routing based on the user's role, ensuring they cannot access pages they shouldn’t.

 Purpose:
   - This file helps manage navigation across different role-specific dashboards (admin, patient, doctor, and logged patient).
   - It ensures that users are sent to their respective dashboard based on their role and token validity.

*/
