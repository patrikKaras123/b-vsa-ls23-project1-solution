package sk.stuba.fei.uim.testing

import java.time.Duration

public class Evaluation {
    public String exam = ''
    public Student student = new Student()
    public String github = ''
    public TestRun required = new TestRun()
    public TestRun bonus = new TestRun()
    public Integer totalPoints = 0
    public String notes = ''
    public Duration testDuration

    TestRun getTotalTestRun() {
        TestRun total = new TestRun(
                totalRun: required.totalRun,
                success: required.success,
                failure: required.failure,
                error: required.error,
                skip: required.skip,
                points: required.points
        )
        total.totalRun += bonus.totalRun
        total.success += bonus.success
        total.failure += bonus.failure
        total.error += bonus.error
        total.skip += bonus.skip
        total.points += bonus.points
        return total
    }

    Integer calcTotalPoints(int max, int bon) {
        required.calcPoints(max.doubleValue())
        bonus.calcPoints(bon.doubleValue())
        totalPoints = required.points + bonus.points
        return totalPoints
    }

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, [
                student.toString(delimiter),
                github,
                required.toString(delimiter),
                bonus.toString(delimiter),
                totalPoints as String,
                notes])
    }

    Node toXml() {
        Node xml = new NodeBuilder().evaluation {
            'exam'(exam)
            'student'('Dummy student')
            'github'(github)
            'testRuns' {
                'required' {
                    'tests'('Tests placeholder')
                }
                'bonus' {
                    'tests'('Tests placeholder')
                }
            }
            'totalPoints'(totalPoints)
            'notes'(notes)
            'testDuration'(testDuration.toString())
        }
        (xml.student[0] as Node).replaceNode(student.toXml())
        (xml.testRuns.required.tests[0] as Node).replaceNode(required.toXml())
        (xml.testRuns.bonus.tests[0] as Node).replaceNode(bonus.toXml())
        return xml
    }
}
