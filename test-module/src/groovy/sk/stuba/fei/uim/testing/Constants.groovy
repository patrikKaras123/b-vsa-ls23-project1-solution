package sk.stuba.fei.uim.testing

class Constants {
    static String CWD = new File('.').absolutePath
    static String TEST_PROJECT = ""
    static String FEEDBACK_DIR = "feedback"
    static String TEST_DIR = String.join(File.separator, ['src', 'test', 'java', 'sk', 'stuba', 'fei', 'uim', 'vsa', 'pr1'])
    static String REPORT_DIR = String.join(File.separator, ['target', 'surefire-reports'])

    static List<String> CSV_HEADER = ['AISID', 'Name', 'Email', 'GitHub', 'Tests Run', 'Succeeded', 'Failures', 'Errors',
                                      'Skipped', 'Points', 'Bonus Tests Run', 'Bonus Succeeded', 'Bonus Failures',
                                      'Bonus Errors', 'Bonus Skipped', 'Bonus Points', 'Total Points', 'Notes']
    static String CSV_DELIMITER = ';'

    static Integer MAX_POINTS = 15
    static Integer MAX_BONUS_POINTS = 3

}
