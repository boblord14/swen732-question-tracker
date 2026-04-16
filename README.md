[![codecov](https://codecov.io/github/boblord14/swen732-question-tracker/graph/badge.svg?token=5ALJ2PRTTR)](https://codecov.io/github/boblord14/swen732-question-tracker)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=boblord14_swen732-question-tracker)](https://sonarcloud.io/summary/new_code?id=boblord14_swen732-question-tracker)

### Project Overview

Question-Tracker is an app designed to enable users to learn topics, share questions, and for teachers to support the growth of their students. 
Features a similar question taking app to Quizlet, with support for google-classroom style classes and graded quizzes. 

What sets Question-Tracker apart from the others is that each question has a series of tags set on it- every time you get a question
wrong, the tags are saved. You can then view a composite calculation of the subjects you struggle with, and then challenge 
yourself to improve by focusing on questions exclusively targeting those struggles. Works on a class wide scale for teachers
and assignments as well. 

### Installation instructions
JavaFX is required, otherwise the latest java version(25) is ideal to run this application. 

Download the source and build using the maven project. Maven will also run tests. 

### Usage 
Run the main function inside `Launcher` to properly launch the program.
Usage is entirely through the GUI, everything is seamless and persistent. 

### Project Structure
`src/main/java/app` is the GUI controller/view files.

`src/main/java/model` are model files.

Anything else in `src/main/java` are basic classes. 

The various json files serve as persistance in plaintext. Data can be viewed at will. 

Anything in `src/test` are junit tests. 

### Technologies Used
JavaFX for the graphics
Junit for testing
Jacoco for code coverage reports
Codecov for uploaded coverage reports
Github for source control

### Contributing Guidelines
Branch first, pass tests, pr, merge to main

### Contact/Report
Issues function on the repo exists for anything needed here. 

