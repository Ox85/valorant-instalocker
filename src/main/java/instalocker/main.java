package instalocker;

import instalocker.utils.files;
import instalocker.valorant.gui;

import java.io.File;

public class main {

    public static final String REPO_OWNER = "YOUR_NAME";
    public static final String REPO_NAME = "YOUR_REPO";
    public static final String REPO_BRANCH = "main";

    public static String program_name = "instalocker";
    public static String program_path = new File(System.getProperty("java.io.tmpdir"), program_name).getAbsolutePath();

    public static String program_data_link =
            "https://raw.githubusercontent.com/" + REPO_OWNER + "/" + REPO_NAME + "/refs/heads/" + REPO_BRANCH + "/data.json";
    public static String assets_link =
            "https://github.com/" + REPO_OWNER + "/" + REPO_NAME + "/raw/refs/heads/" + REPO_BRANCH + "/assets.zip";

    public static void main(String[] args) {
        File assetsZip = new File(System.getProperty("java.io.tmpdir"), "assets.zip");
        files.downloadFile(assets_link, assetsZip);
        files.unzip(assetsZip.getAbsolutePath(), program_path);
        assetsZip.delete();

        new gui();
    }
}
