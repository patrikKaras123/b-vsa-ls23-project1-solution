package sk.stuba.fei.uim.testing

public class TestRun {
    public Integer totalRun = 0
    public Integer success = 0
    public Integer failure = 0
    public Integer error = 0
    public Integer skip = 0
    public Integer points = 0

    public void calcPoints(Double maxPoints) {
        if (success == 0)
            calcSuccess()
        points = Math.ceil((maxPoints / (totalRun.doubleValue() - skip.doubleValue())) * success.doubleValue()).intValue()
    }

    public void calcSuccess() {
        success = totalRun - (failure + error + skip)
    }

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, [
                totalRun as String,
                success as String,
                failure as String,
                error as String,
                skip as String,
                points as String
        ])
    }

    Node toXml() {
        return new NodeBuilder().testRun {
            'totalRun'(totalRun)
            'success'(success)
            'failure'(failure)
            'error'(error)
            'skip'(skip)
            'points'(points)
        } as Node
    }

    TestRun evaluateTests(File surefireReport, boolean bonusTests){
        surefireReport.eachLine { line ->
            if (!line.startsWith('Tests run:')) return
            String[] parts = line.split(',')
            this.totalRun += Integer.parseInt(parts[0].substring(parts[0].lastIndexOf(' ')).trim())
            this.failure += Integer.parseInt(parts[1].substring(parts[1].lastIndexOf(' ')).trim())
            this.error += Integer.parseInt(parts[2].substring(parts[2].lastIndexOf(' ')).trim())
            this.skip += Integer.parseInt(parts[3].substring(parts[3].lastIndexOf(' ')).trim())

        }
        int maxPoints = bonusTests ? Constants.MAX_BONUS_POINTS : Constants.MAX_POINTS
        this.calcSuccess()
        this.calcPoints(maxPoints.doubleValue())
        return this
    }
}
