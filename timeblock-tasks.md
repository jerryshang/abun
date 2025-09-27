# Timeblock Planning and Logging Feature

## Overview
A comprehensive timeblock planning and logging system with navigation, zoom controls, date selection, and timeblock management capabilities.

## Database Schema

### Tables to Create
- [x] **timeblock** - Main timeblock entries
  - id (INTEGER PRIMARY KEY)
  - start_time (TEXT) - ISO timestamp
  - end_time (TEXT) - ISO timestamp
  - task_id (INTEGER) - Foreign key to task
  - created_at (TEXT) - ISO timestamp
  - updated_at (TEXT) - ISO timestamp

- [x] **task** - Tasks that can be assigned to timeblocks
  - id (INTEGER PRIMARY KEY)
  - name (TEXT)
  - description (TEXT)
  - category_id (INTEGER) - Foreign key to category
  - created_at (TEXT)
  - updated_at (TEXT)

- [x] **category** - Categories for organizing tasks
  - id (INTEGER PRIMARY KEY)
  - name (TEXT)
  - color (TEXT) - Hex color code
  - created_at (TEXT)
  - updated_at (TEXT)

- [x] **alarm** - Alarms for future timeblocks
  - id (INTEGER PRIMARY KEY)
  - timeblock_id (INTEGER) - Foreign key to timeblock
  - minutes_before (INTEGER) - Minutes before timeblock start
  - is_enabled (INTEGER) - Boolean (0/1)
  - created_at (TEXT)

## Implementation Tasks

### Phase 1: Database Setup
- [ ] Create database schema files (.sq)
- [ ] Add database models/entities
- [ ] Update database configuration
- [ ] Test database setup with build

### Phase 2: Core UI Structure
- [ ] Add timeblock navigation icon to main navigation
- [ ] Create TimeblockScreen composable
- [ ] Implement function bar with zoom switcher, date picker, create button
- [ ] Create basic time slot list (30-minute intervals)
- [ ] Test UI structure with build

### Phase 3: Data Layer
- [ ] Create repository interfaces
- [ ] Implement database repositories
- [ ] Create ViewModels for timeblock management
- [ ] Add dependency injection setup
- [ ] Test data layer with build

### Phase 4: Timeblock Creation
- [ ] Implement timeblock creation dialog
- [ ] Add task selection/creation in timeblock dialog
- [ ] Implement category selection
- [ ] Add validation for timeblock creation
- [ ] Test timeblock creation with build

### Phase 5: Timeline Display
- [ ] Implement timeblock rendering in time slots
- [ ] Add category color coding
- [ ] Implement semi-transparency for future timeblocks
- [ ] Add timeblock modification functionality
- [ ] Test timeline display with build

### Phase 6: Alarm System
- [ ] Implement alarm creation for future timeblocks
- [ ] Add multiple alarm support (1 day, 30 min, 1 min before)
- [ ] Create alarm management UI
- [ ] Add alarm persistence
- [ ] Test alarm system with build

### Phase 7: Advanced Features
- [ ] Implement zoom functionality (full day/4 hours)
- [ ] Add date navigation
- [ ] Implement timeblock editing
- [ ] Add timeblock deletion
- [ ] Test complete feature with build

### Phase 8: Polish & Testing
- [ ] Add error handling
- [ ] Improve UI/UX
- [ ] Add loading states
- [ ] Final testing and bug fixes
- [ ] Complete build verification

## Technical Notes
- Use SQLDelight for database operations
- Follow existing Koin DI patterns
- Use Compose Material3 for UI consistency
- Implement proper state management with ViewModels
- Use existing navigation patterns