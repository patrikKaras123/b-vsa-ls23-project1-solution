package sk.stuba.fei.uim.testing

import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileUtils {

    static int purge(String path) {
        return purge(new File(path))
    }

    static int purge(File file) {
        if (file.isFile()) {
            file.delete()
            return 1
        } else if (file.isDirectory()) {
            File[] files = file.listFiles()
            int deleteCount = 0
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteCount += purge(f)
                    f.deleteDir()
                    deleteCount++
                    continue
                }
                f.delete()
                deleteCount++
            }
            return deleteCount
        } else {
            return 0
        }
    }

    static void copyDir(File from, File to, boolean recursively = false) {
        if (!from.isDirectory()) return
        to.mkdirs()
        File[] files = from.listFiles()
        for (File file : files) {
            if (file.isDirectory()) {
                if (recursively) {
                    copyDir(file, new File(to.absolutePath + File.separator + file.getName()), true)
                }
                continue
            }
            File target = new File(to.absolutePath + File.separator + file.getName())
            Files.copy(
                    file.toPath(),
                    target.toPath(),
                    StandardCopyOption.COPY_ATTRIBUTES,
                    StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

}
