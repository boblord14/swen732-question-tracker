# SWEN-732 Question Tracker

A small Java project for tracking question classes, users, and basic classroom operations. This repo is an early prototype used for coursework and demonstrates file-backed persistence (JSON) with simple in-memory models.

## Key features (current)

- Models
  - `User` (`src/main/java/user/User.java`) — id, username, password, isTeacher; plain getters/setters
  - `Question` (`src/main/java/user/Question.java`) — placeholder (id, text)
  - `Classroom` (`src/main/java/user/Classroom.java`) — name, code, teacher (single `User`), students (List<User>), questions (List<Question>); helpers: `addStudent`, `removeStudent`, `addQuestion`, `checkCode`

- Core app (`src/main/java/questionTracker.java`)
  - User persistence: `getUsers()` / `saveUsers()` read/write `src/main/users.json` (Jackson)
  - Auth utilities: `logIn(username,password)`, `signUp(username,password,isTeacher)`
  - Classroom persistence: `getClasses()` / `saveClasses()` read/write `src/main/classes.json`
  - Classroom ops: `createClass(name,code,teacher)` (teacher-only, unique code) and `joinClass(student,code)` (student joins when code matches)

## Files to know

- `src/main/users.json` — users are stored here as a JSON array of `User` objects. If missing, methods return an empty list.
- `src/main/classes.json` — classrooms are stored here. Creating/joining classes will read/write this file. An initial empty file containing `[]` is recommended.

## Build & run

This is a Maven project. To compile (skip tests for speed):

```bash
mvn -DskipTests package
```

Run the small demo main in `questionTracker` (from IDE or with `java -cp target/classes questionTracker` after building). The `main` currently calls a demo login and prints results.

## Usage snippets (programmatic)

- Create a teacher and a class (programmatically):
  - Use `signUp(username,password,true)` to create a teacher user.
  - Call `createClass(name, code, teacherUser)` to create a class.

- Student joins a class:
  - Use `signUp(username,password,false)` to create a student user.
  - Call `joinClass(studentUser, code)` to join if the code matches.

## Known limitations

- Passwords are stored and compared as plain text (no hashing). Do not use real credentials.
- `User` does not override `equals()`/`hashCode()`; equality is by object identity. This can cause duplicated logical users when different `User` instances represent the same logical user.
- File-based JSON persistence has no locking — concurrent updates may conflict.
- No API layer or UI; operations are programmatic.

## Suggested next steps

- Add `equals()` / `hashCode()` to `User` (use `id` or `username`) to prevent duplicate students.
- Hash/salt passwords before saving and compare hashed values on login.
- Add basic unit tests for classroom creation/joining and code-check behavior.
- Create `src/main/classes.json` with `[]` to make first run explicit.
- Consider a lightweight DB (SQLite) or add file locking to avoid race conditions.
- Expose a small REST API (Spring Boot) or CLI for easier interaction.

## Contact / notes

This project was developed as part of an academic assignment. If you want, I can:

- run a build and report any compiler errors,
- add an initial `src/main/classes.json` file,
- implement `equals`/`hashCode` for `User`, or
- add tests for class features.

Tell me which you'd like and I'll do it next.
