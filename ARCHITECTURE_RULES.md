# Game Project Architecture Rules

This file describes the architectural rules for a new project built using the same design principles as the existing EmbeddedSystems-SystemSkeleton.

## 1. Use a Central Router for UI Commands
- The UI must never call business logic directly.
- All UI actions must go through a central `MainRouter`.
- The router receives a path string and parameters, then forwards the request to the correct `SubRouter`.
- Example: `mainRouter.route("/ex/point/move", Params.of(...))`

## 2. Keep UI and Logic Separated
- UI classes should only handle rendering and user input.
- The UI must not modify the business model directly.
- All user interactions must be translated into router requests.
- Example: drag events in the UI should call the router, not update the backend model directly.

## 3. Use SubRouters for Domain-Specific Routing
- Each domain or feature area should have its own `SubRouter` implementation.
- The main router delegates to sub-routers based on the path prefix.
- Example prefix: `/ex` for the Ex feature.

## 4. Put Business Logic in Backend Classes
- Backend classes should own the application logic and state changes.
- Backend methods should be invoked by routers, not by UI components.
- Backend classes should use UI ports to send update commands to the UI.

## 5. Use an Abstract UI Port for UI Commands
- Define an abstract UI port interface or base class in a shared package.
- The backend should only call this abstract UI port.
- The UI should provide a concrete implementation that updates the UI view.
- This preserves a clean boundary between logic and presentation.

## 6. Initialize the System from a Single Entry Point
- Use a central class such as `App` or `Main` to wire the system together.
- The initialization order should typically be:
  1. initialize shared content/model
  2. create the UI
  3. set UI ports
  4. register sub-routers in the main router
  5. start the UI with the main router
  6. start any periodic scheduler if needed

## 7. Do Not Expose UI Objects to Backend
- The backend should never receive UI-specific objects such as panels, frames, or components.
- UI communication must happen only through the abstract UI port methods.
- Example: backend should call `exUiPort().updatePoint(id, x, y)` rather than manipulating UI components directly.

## 8. Use Clear and Consistent Route Paths
- Define route paths with a consistent structure.
- Use action names and hierarchical segments.
- Examples:
  - `/ex/start`
  - `/ex/point/move`
  - `/ex/circle/add`
  - `/ex/reset`

## 9. Keep Models in Shared Content
- Use a shared content or model container for application state.
- Access the model through a central content class, not through UI classes.
- Example: `App.content().canvas()`.

## 10. Maintain One-Way Data Flow from Logic to UI
- UI input flows to router → router flows to backend → backend updates model and issues UI commands.
- UI rendering is driven by the UI port implementation.
- This ensures no direct two-way coupling between UI and business logic.

## Summary
The new Game project should follow these architectural rules to preserve separation of concerns, enforce routing, and avoid direct UI-to-logic coupling. The same structure can be reused for new features while keeping the UI, routers, backend logic, and shared model clearly separated.