# 1. Code Coverage
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
    <link rel="stylesheet" href="jacoco-resources/report.css" type="text/css" />
    <link rel="shortcut icon" href="jacoco-resources/report.gif" type="image/gif" />
    <title>Question Tracker</title>
    <script type="text/javascript" src="jacoco-resources/sort.js"></script>
</head>

<body onload="initialSort(['breadcrumb', 'coveragetable'])">

    <div class="breadcrumb" id="breadcrumb">
        <span class="info">
            <a href="jacoco-sessions.html" class="el_session">Sessions</a>
        </span>
        <span class="el_report">Question Tracker</span>
    </div>

    <h1>Question Tracker</h1>

    <table class="coverage" cellspacing="0" id="coveragetable">
        <thead>
            <tr>
                <td class="sortable" id="a" onclick="toggleSort(this)">Element</td>
                <td class="down sortable bar" id="b" onclick="toggleSort(this)">Missed Instructions</td>
                <td class="sortable ctr2" id="c" onclick="toggleSort(this)">Cov.</td>
                <td class="sortable bar" id="d" onclick="toggleSort(this)">Missed Branches</td>
                <td class="sortable ctr2" id="e" onclick="toggleSort(this)">Cov.</td>
                <td class="sortable ctr1" id="f" onclick="toggleSort(this)">Missed</td>
                <td class="sortable ctr2" id="g" onclick="toggleSort(this)">Cxty</td>
                <td class="sortable ctr1" id="h" onclick="toggleSort(this)">Missed</td>
                <td class="sortable ctr2" id="i" onclick="toggleSort(this)">Lines</td>
                <td class="sortable ctr1" id="j" onclick="toggleSort(this)">Missed</td>
                <td class="sortable ctr2" id="k" onclick="toggleSort(this)">Methods</td>
                <td class="sortable ctr1" id="l" onclick="toggleSort(this)">Missed</td>
                <td class="sortable ctr2" id="m" onclick="toggleSort(this)">Classes</td>
            </tr>
        </thead>

        <tfoot>
            <tr>
                <td>Total</td>
                <td class="bar">711 of 2,063</td>
                <td class="ctr2">65%</td>
                <td class="bar">131 of 240</td>
                <td class="ctr2">45%</td>
                <td class="ctr1">121</td>
                <td class="ctr2">194</td>
                <td class="ctr1">178</td>
                <td class="ctr2">463</td>
                <td class="ctr1">26</td>
                <td class="ctr2">74</td>
                <td class="ctr1">0</td>
                <td class="ctr2">7</td>
            </tr>
        </tfoot>

        <tbody>
            <tr>
                <td id="a0">
                    <a href="model/index.html" class="el_package">model</a>
                </td>
                <td class="bar" id="b0">
                    <img src="jacoco-resources/redbar.gif" width="41" height="10" title="711" alt="711" />
                    <img src="jacoco-resources/greenbar.gif" width="78" height="10" title="1,352" alt="1,352" />
                </td>
                <td class="ctr2" id="c0">65%</td>
                <td class="bar" id="d0">
                    <img src="jacoco-resources/redbar.gif" width="65" height="10" title="131" alt="131" />
                    <img src="jacoco-resources/greenbar.gif" width="54" height="10" title="109" alt="109" />
                </td>
                <td class="ctr2" id="e0">45%</td>
                <td class="ctr1" id="f0">121</td>
                <td class="ctr2" id="g0">194</td>
                <td class="ctr1" id="h0">178</td>
                <td class="ctr2" id="i0">463</td>
                <td class="ctr1" id="j0">26</td>
                <td class="ctr2" id="k0">74</td>
                <td class="ctr1" id="l0">0</td>
                <td class="ctr2" id="m0">7</td>
            </tr>
        </tbody>
    </table>

    <div class="footer">
        <span class="right">
            Created with <a href="http://www.jacoco.org/jacoco">JaCoCo</a> 0.8.11.202310140853
        </span>
    </div>

</body>
</html>

# Project Overview:
## A program that helps a student review for a subject. Either by reviewing questions the student
## made themselves or joining classes a teacher created and taking practice quizzes the teacher assigned.
## Additionally this project can recommend questions based on what subjects you are struggling with.

# Installation Instructions:
## JDK 17 or Later
## JavaFX 21
## Apache Maven 3.8+
## commons-math3
## jackson-databind
## javafx-controls
## javafx-fxml

# Project Structure:
## src/main/java/model- Holds the backend applications
## src/main/java/app- Holds the frontend applications
## src/main/java/question- Holds the class used when making Questions
## src/main/java/teacher- Holds the class for making a Studyset (sets of questions that are meant to quiz students)
## src/main/java/user- Holds the classes used by the general user: Classroom, Question, QuestionSet(Sets made by the user for themselves),
##      User, UserPrediction(The users question data based on answers to past questions)
## src/test/java- The tests ran to test the application and verify code coverage.

# Contributing Guidelines: 
##      1. Create a branch off of an existing branch
##      2. When finished push changes as a merge request
##      3. Wait for merge request to be approved or rejected

# Contact or Support Information:
## If you have any questions or concerns please email: kjk7134@rit.edu