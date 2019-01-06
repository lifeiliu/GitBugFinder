import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BugsPerFile {


    public static void main(String[] args) throws IOException, GitAPIException {
        String repoPath = "/home/ggff/workplace/crawler4j";
        List<File> javaFiles = new ArrayList<>();
        getAllJavaFiles(new File(repoPath), javaFiles);
        HashMap<String, Integer> fileChangeCounter = new HashMap<>();

        Git git = Git.open(new File(repoPath + "/.git"));
        Repository repo = git.getRepository();
        Iterable<RevCommit> commits = git.log().call();
        List<RevCommit> bugFixCommits = new ArrayList<>();
        for (RevCommit each : commits) {
            String commitMsg = each.getFullMessage();
            if (commitMsg.toLowerCase().contains("fix") || commitMsg.toLowerCase().contains("bug")) {
                bugFixCommits.add(each);
            }
        }

        for (RevCommit each : bugFixCommits){
            System.out.println(each.getFullMessage());
            System.out.println("=====================");
            System.out.println("files changed: ");

            RevTree tree = each.getTree();
            TreeWalk treeWalk = new TreeWalk(repo);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()){
                String path = treeWalk.getPathString();
                if(!path.contains("src/test") && ! path.contains("crawler4j-examples") && path.endsWith(".java") ){
                    String fileName = treeWalk.getNameString();
                    if (!fileChangeCounter.containsKey(fileName))
                        fileChangeCounter.put(fileName, new Integer(1));
                    else {
                        int currentValue = fileChangeCounter.get(fileName).intValue();
                        fileChangeCounter.replace(fileName, new Integer(currentValue + 1));
                    }
                }
                //System.out.println(treeWalk.getNameString());

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
        System.out.println(fileChangeCounter.toString());
        git.close();

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
