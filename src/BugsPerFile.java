import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BugsPerFile {

    public static Git git;
    public static void main(String[] args) throws IOException, GitAPIException {

        String repoPath = "C:\\Users\\lifei\\Desktop\\thesis\\androidSystemGit\\base";
        List<File> javaFiles = new ArrayList<>();
        getAllJavaFiles(new File(repoPath), javaFiles);
        HashMap<String, Integer> fileChangeCounter = new HashMap<>();

        git = Git.open(new File(repoPath + "/.git"));
        Repository repo = git.getRepository();
        Iterable<RevCommit> commits = git.log().addPath("core/java/android/app/ActivityThread.java").call();
        List<RevCommit> bugFixCommits = new ArrayList<>();
        for (RevCommit each : commits) {
            String commitMsg = each.getFullMessage();
            if (commitMsg.toLowerCase().contains("fix") || commitMsg.toLowerCase().contains("bug") || !commitMsg.contains("Merge")) {
                bugFixCommits.add(each);
            }
        }

        /*for (File file : javaFiles) {
            System.out.println(file.getName());
            System.out.println("================================================");
            Iterable<RevCommit> commits = git.log().all().addPath(file.getPath()).call();
            for (RevCommit each : commits) {
                String commitMsg = each.getFullMessage();
                if (commitMsg.toLowerCase().contains("fix") || commitMsg.toLowerCase().contains("bug")) {
                    System.out.println(each);
                }
            }
            System.out.println("================================================");
        }*/

        System.out.println(bugFixCommits.size());
        for (RevCommit each : bugFixCommits){
            getDiff(each);
        }

        //System.out.println(fileChangeCounter.toString());
        git.close();

    }
    private static void getDiff(RevCommit newCommit) throws IOException, GitAPIException {
        RevCommit prevCommit = newCommit.getParent(0);
        if (prevCommit == null){
            System.out.println("start of repo");
        }
        Repository repo = git.getRepository();
        ObjectReader reader = repo.newObjectReader();
        AbstractTreeIterator oldTree = new CanonicalTreeParser(null, reader,prevCommit.getTree()) ;
        AbstractTreeIterator newTree = new CanonicalTreeParser(null,reader,newCommit.getTree());
        /*OutputStream outputStream = new ByteArrayOutputStream();
        DiffFormatter diffFormatter = new DiffFormatter(outputStream);
        diffFormatter.setRepository(repo);
        diffFormatter.format(oldTree,newTree);*/

        List<DiffEntry> entries = git.diff()
                .setNewTree(newTree)
                .setOldTree(oldTree)
                .call();

        for(DiffEntry each : entries){
           String newPath = each.getNewPath();
           if("ActivityThread.java".equals(getNameFromPath(newPath))){
               DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
               formatter.setRepository(git.getRepository());
               FileHeader fileHeader = formatter.toFileHeader(each);
                //Todo: map changed lines to methods names.
               System.out.println(fileHeader.toEditList());
           }
        }
    }

    private static String getNameFromPath(String pathName){
        int start = pathName.lastIndexOf('/');
        return pathName.substring(start+1);
    }

    private static void getAllJavaFiles (File folder, List<File> result){
        File[] files = folder.listFiles();
        for(File f : files){
            if(f.isDirectory()){
                getAllJavaFiles(f,result);
            }else{
                if(f.getName().toLowerCase().endsWith(".java"))
                    result.add(f);
            }
        }
    }
}
