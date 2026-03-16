
# Question Tracker Design Documentation


## Team Information
* Team name: Question-Tracker
* Team members
  * Ethan Patterson
  * Kyle Kline
  * Branden Mccolm
  * Grahith Movva
  * Raymond Babich

## Executive Summary

A quiz-taking software where users can practice skills and topics, through working on a series of
questions. When a user gets a question right/wrong, the exact skills/topics relevant for that
question are saved and tracked. As the user goes through a significant number of questions,
this enables the system to compile data on what the user is proficient at, and the topics/skills
they struggle on. The system is then capable of curating the question feed to the user in order to
specifically target and improve their skills on their areas of struggle.

Question sets can be created by all users, and individual questions can be tagged with a series
of tags to mark what specific tags a question covers, to enable the system to learn from a user.
Teachers can also assign question sets to students, and be able to see their results and specific
areas of struggle/strength.

3 distinct roles:
Student/quiz taker- uses software to focus on improving their skills in a given subject area
Question list creator- uses software to create question lists for self and/or others
Teacher- uses software to assign question sets to students and see their result data(can also be
a question list creator)



## Requirements

This section describes the features of the application.

### Definition of MVP
Working product that fullfills the needs and requirements of each of the 3 distinct roles. Testing is performed to ensure a quality product, and has a working gui for the user. 

### MVP Features

User stories: 
- As a student, I want to review practice material on a subject so that I can prepare for class.
- As a student, I want to see which subjects I'm struggling with the most so that I know which subjects I should focus on.
- As a student, I want to select tags on questions so that the system can assign questions based on struggles.
- As a student, I want to create an account so I can use the software and join my teacher's class.
- As a student, I want to login to the site so that I can resume my progress.
- As a student, I want to search for practice materials so that I can easily find materials to study from.
- As a teacher, I want to view my students' progress so that I can determine how to help them.
- As a teacher, I want to create study materials for classes so that students can get better grades.
- As a teacher, I want to be able to invite students to my class via a code so that I can assign them questions and see their results.
- As a question creator, I want to create/add tags to subjects and questions I create so they're easier to find and filter.
- As a teacher, I want to be able to assign question sets to students so that I can test my students.


## Architecture and Design

This section describes the application architecture.

### Software Architecture

Model-View-Controller Architecture

![alt text](<Architectural Diagram Swen 732-Architectural Diagram.drawio.png>)

### Use Cases

![alt text](<Architectural Diagram Swen 732-Use Case Diagram.drawio.png>)