/* 
 Function Overview:
   - `setRole(role)`: Stores the user's role in `localStorage`.
   - `getRole()`: Retrieves the user's role from `localStorage`.
   - `clearRole()`: Removes the user's role from `localStorage`.

 Functions Breakdown:
   - **`setRole(role)`**:
     - **Purpose**: Stores the user's role in `localStorage`. This is typically called after a user logs in to set their role (e.g., admin, patient, doctor).
     - **Input**: 
       - `role` (String): The role of the user. Examples: "admin", "patient", "doctor".
     - **Output**: No return value. This function simply stores the role in the `localStorage` under the key `"userRole"`.
  
   - **`getRole()`**:
     - **Purpose**: Retrieves the current user's role from `localStorage`.
     - **Input**: None.
     - **Output**: A string representing the user's role (e.g., "admin", "patient", "doctor") or `null` if no role is set.
     - **Use Case**: This function is useful for checking the user's role to determine what kind of content to display or which page to redirect them to.
  
   - **`clearRole()`**:
     - **Purpose**: Clears the user's role from `localStorage`. This is useful for logging out users and removing any session data.
     - **Input**: None.
     - **Output**: No return value. This function simply removes the role from `localStorage` using the key `"userRole"`.
     - **Use Case**: This function is typically called during the logout process to clear all session-related data.

 Key Points:
   - **`localStorage`**: This browser feature allows you to store key-value pairs that persist across page reloads and browser sessions. It is used here to store the user's role information.
   - **Role Management**: By using these functions, the application can easily manage user access and functionality by checking the role stored in `localStorage`. This can determine which sections of the app a user has access to (e.g., admin dashboard, patient dashboard, etc.).

 Best Practices:
   - Ensure the role is set after a successful login, and the role check is done before rendering any sensitive content based on the user's permissions.
   - Use `clearRole()` on logout to remove any potentially sensitive session information.

*/
