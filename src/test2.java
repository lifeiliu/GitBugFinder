import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class test2 {

    public static HashMap<String, Integer> changedFiles;
    public static void main(String [] args){
        changedFiles = new HashMap<>();
        try{
            Git git = Git.open(new File("E:\\gitProject\\crawler4j\\.git"));
            //Git git = Git.cloneRepository().setURI("https://github.com/apache/maven.git").setDirectory(new File("E:\\gitProject\\maven")).call();
            commitHistory(git);
            git.close();
            System.out.println(changedFiles);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoHeadException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    private static void updateBugChangeFiles(String fileName, HashMap<String, Integer> changeFils){
        if (changeFils.containsKey(fileName)){
            int oldVal = changeFils.get(fileName);
            int newVal = oldVal + 1;
            changeFils.replace(fileName, new Integer(newVal));
        }else
            changeFils.put(fileName,new Integer(1));
    }

    public static void commitHistory(Git git) throws NoHeadException, GitAPIException, IncorrectObjectTypeException, CorruptObjectException, IOException    {
        Iterable<RevCommit> logs = git.log().call();
        int k = 0;
        for (RevCommit commit : logs) {
            String commitMsg = commit.getFullMessage();
            String commitID = commit.getName();

            if (commitID != null && !commitID.isEmpty() &&(commitMsg.toLowerCase().contains("fix") || commitMsg.toLowerCase().contains("bug")))
            {
                System.out.println("bug commit: "+commitID);
                System.out.println(commitMsg);
                LogCommand logs2 = git.log().all();
                Repository repository = logs2.getRepository();
                TreeWalk tw = new TreeWalk(repository);
                tw.setRecursive(true);
                RevCommit commitToCheck = commit;
                tw.addTree(commitToCheck.getTree());
                for (RevCommit parent : commitToCheck.getParents())
                {
                    tw.addTree(parent.getTree());
                }
                while (tw.next())
                {
                    int similarParents = 0;
                    for (int i = 1; i < tw.getTreeCount(); i++)
                        if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
                            similarParents++;
                    if (similarParents == 0){
                        String fileName = tw.getNameString();
                        if (fileName.endsWith(".java"))
                            System.out.println(fileName);
                            updateBugChangeFiles(fileName,changedFiles);
                    }

                }
            }
        }
    }
}
