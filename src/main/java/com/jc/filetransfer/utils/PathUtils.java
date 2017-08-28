package com.jc.filetransfer.utils;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;

public class PathUtils {

    /**
     * 保证目录路径的结尾以文件分隔符结束,路径的长度不能小于0
     *
     * @param dirPath
     * @return
     */
    public static String adjustDirPath(String dirPath) {
        dirPath = StringUtils.trim(dirPath);
        Preconditions.checkArgument(null != dirPath && dirPath.length() > 0,
                "Please check your directory path:" + dirPath);

        char lastChar = dirPath.charAt(dirPath.length() - 1);
        if (lastChar != File.separatorChar)
            return dirPath + File.separatorChar;
        return dirPath;
    }

    /**
     * 拼接出一个文件路径，两个路径的长度都不能小于0
     *
     * @param baseDir                    ,一个目录路径
     * @param relativeFilePath，相对路径的文件路径
     * @return
     */
    public static String concatFilePath(String baseDir, String relativeFilePath) {
        relativeFilePath = StringUtils.trim(relativeFilePath);

        Preconditions.checkArgument(null != relativeFilePath
                        && relativeFilePath.length() > 0
                        && relativeFilePath.charAt(0) != File.separatorChar
                        && relativeFilePath.charAt(relativeFilePath.length() - 1) != File.separatorChar
                , "Please check your relative file path:" + relativeFilePath);

        baseDir = adjustDirPath(baseDir);
        return baseDir + relativeFilePath;
    }

    /**
     * 拼接出一个目录路径
     *
     * @param baseDir         ，目录路径
     * @param relativeDirPath ，相对路径的目录路径
     * @return
     */
    public static String concatDirPath(String baseDir, String relativeDirPath) {
        relativeDirPath = StringUtils.trim(relativeDirPath);

        Preconditions.checkArgument(null != relativeDirPath
                        && relativeDirPath.length() > 0
                        && relativeDirPath.charAt(0) != File.separatorChar
                , "Please check your relative directory path:" + relativeDirPath);

        baseDir = adjustDirPath(baseDir);
        return adjustDirPath(baseDir + relativeDirPath);
    }


    /**
     * 拼接多个路径成为一个目录路径
     *
     * @param baseDir          ，目录路径
     * @param relativeDirPaths ，多个目录路径
     * @return
     */
    public static String concatDirPath(String baseDir, String... relativeDirPaths) {
        Preconditions.checkArgument(null != relativeDirPaths && relativeDirPaths.length > 0,
                "Please check your relative paths:" + relativeDirPaths);

        String retDir = baseDir;
        for (String dir : relativeDirPaths)
            retDir = concatDirPath(retDir, dir);

        return retDir;
    }


    /**
     * 拼接出一个文件路径
     *
     * @param baseDir
     * @param relativePaths ，长度为n的数组(n>0),0-(n-2)为目录路径，n-1为文件路径
     * @return
     */
    public static String concatFilePath(String baseDir, String... relativePaths) {
        Preconditions.checkArgument(null != relativePaths && relativePaths.length > 0,
                "Please check your relative paths:" + relativePaths);

        String dirPath = concatDirPath(baseDir, Arrays.copyOf(relativePaths, relativePaths.length - 1));
        return concatFilePath(dirPath, relativePaths[relativePaths.length - 1]);
    }

}
