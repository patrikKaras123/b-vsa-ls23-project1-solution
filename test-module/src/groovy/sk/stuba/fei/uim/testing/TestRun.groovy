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
        return new NodeBuilder().testsRun {
            'totalRun'(totalRun)
            'success'(success)
            'failure'(failure)
            'error'(error)
            'skip'(skip)
            'points'(points)
        } as Node
    }
}
