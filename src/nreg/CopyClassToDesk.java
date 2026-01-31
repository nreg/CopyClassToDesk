package nreg;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Title:
 * Description:
 * Author: nreg
 * CreateDate: 2020/1/6 22:38
 * Modify User:
 * Modify Date:
 * Modify Description:
 */
public class CopyClassToDesk extends AnAction {
    private Project mProject;
    @Override
    public void actionPerformed(AnActionEvent event) {
        mProject = event.getData(PlatformDataKeys.PROJECT);
        //VirtualFile file = (VirtualFile) event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE.getName());
        VirtualFile proj = (VirtualFile) event.getDataContext().getData(PlatformDataKeys.PROJECT_FILE_DIRECTORY.getName());


        if (proj == null) {
            return;
        }

        //当前项目根路径(最后不含/)
        String basePath = proj.getPath();

        //获取当前项目的名称
        String projectName = basePath.replaceAll(".+/", "");

        //获取当前系统的桌面路径
        String desktopPath =  FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();

        //创建输出文件夹的路径
        String outDirectory=desktopPath+"/"+projectName;

        //假设的编译文件夹的路径
        String middlePath = "";

        //假设编译文件夹是target
        String hypothesisTarget = basePath + "/target";
        File isTarget = new File(hypothesisTarget);
        if (isTarget.exists() && isTarget.isDirectory()) {
            middlePath = hypothesisTarget;
        }

        //假设编译文件夹是out
        String hypothesisOut = basePath + "/target";
        File isOut = new File(hypothesisOut);
        if (isOut.exists() && isOut.isDirectory()) {
            middlePath = hypothesisOut;
        }

        //判断当前项目下是否有模块
        String hypothesHas = basePath + "/target/production";
        File isHas = new File(hypothesHas);
        if (isHas.exists() && isHas.isDirectory()) {
            //middlePath = hypothesHas;目前无法获取当前模块的名称
            return;
        }


        //判断当前选中的文件是否是java文件
        DataContext dataContext = event.getDataContext();
        if ("java".equals(getFileExtension(dataContext))) {

            //获取选中的文件
            VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            if (file != null) {
                String tierPath = getPackage(file);//获取文件的父路径限制到src/main/java包下的第1个子包开始到类所在的最后一个包名结束（需要创建的目录的层级）

                //创建
                String fileName = replaceFileExtension(file);//更改文件名的后缀：把.java文件的后缀改成.class并返回
                //当前选中类的编译文件
                String sourcePath=middlePath+"/classes/"+tierPath+"/";
                //导出的文件
                String destPath=outDirectory+"/WEB-INF/classes/"+tierPath+"/";

                //创建层级文件夹
                File dir = new File(destPath);
                if (!dir.exists()) {
                    //mkdirs方法在创建当前目录时，会自动创建所有不存在的父目录
                    dir.mkdirs();
                }

                //统一修正/为\
                String source=sourcePath.replace("/","\\")+fileName;
                String dest=destPath.replace("/","\\")+fileName;

                try {
                  copyFile(source,dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        //在Action显示之前,根据选中文件扩展名判定是否显示此Action
        String extension = getFileExtension(event.getDataContext());
        this.getTemplatePresentation().setEnabled(extension != null && "java".equals(extension));
    }

    //判断当前选中的文件的扩展名
    public static String getFileExtension(DataContext dataContext) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(dataContext);
        return file == null ? null : file.getExtension();
    }

    //获取文件的父路径限制到src/main/java包下的第1个子包开始到类所在的最后一个包名结束（需要创建的目录的层级）
    private static String getPackage(VirtualFile file) {
        if (!file.isDirectory()) {
            return getPackage(file.getParent());
        }
        if (file.getParent().getName().equals("src")) {
            return file.getName();
        }
        if (file.getParent().getName().equals("main")) {
            return file.getName();
        }
        if (file.getParent().getName().equals("java")) {
            return file.getName();
        }
        return getPackage(file.getParent()) + "/" + file.getName();
    }

    //把.java文件的后缀改成.class并返回
    private static String replaceFileExtension(VirtualFile file) {
        if ("java".toUpperCase().equalsIgnoreCase(file.getExtension())) {
            return file.getName().replaceAll(file.getExtension(), "class");
        }
        return file.getName();
    }


    /**
     * 创建同名同类型文件，复制其中内容
     * @param sourceFilePath
     * @param destFilePath
     * @throws IOException
     */
    private static void copyFile(String sourceFilePath, String destFilePath) throws IOException {
        if (isNotEmpty(sourceFilePath)&&isNotEmpty(destFilePath)) {
            String sourcePath = URLDecoder.decode(sourceFilePath, "UTF-8");
            String destPath = URLDecoder.decode(destFilePath, "UTF-8");
            File sfile= new File(sourcePath);

            FileInputStream fis = new FileInputStream(sfile);
            //创建新的文件，保存复制内容
            File dfile = new File(destPath);
            //创建同名空文件
            if (!dfile.exists()) {
                dfile.createNewFile();
            }

            //向空文件中写入源文件内容
            FileOutputStream fos = new FileOutputStream(dfile);
            // 读写数据
            // 定义数组
            byte[] b = new byte[1024];
            // 定义长度
            int len;
            // 循环读取
            while ((len = fis.read(b)) != -1) {
                // 写出数据
                fos.write(b, 0, len);
            }
            //关闭资源
            fos.close();
            fis.close();
        }
    }

    //字符串非空判断
    private static boolean isNotEmpty(String str) {
        return (null != str && "".equals(str.trim()) == false && str.length() > 0);
    }
}
