import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

//like git log -p [commitSHA]
public class TestDiff {
    private  static Git git;
    private static Repository repo;

    public static void main(String[] args){
        try {
            git = Git.open(new File("E:\\gitProject\\crawler4j\\.git"));
            repo = git.getRepository();
            Iterable<RevCommit> logs = git.log().call();
            for (RevCommit commit : logs){
                String commitMsg = commit.getFullMessage();
                if (commitMsg.contains("Fix")|| commitMsg.contains("fix") || commitMsg.contains("bug")){
                    System.out.println(commit);
                    System.out.println(commitMsg);
                    getDiff(commit);
                   /* ObjectReader reader = repo.newObjectReader();
                    RevCommit parent = commit.getParent(0);
                    if (parent != null){
                        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                        oldTreeIter.reset(reader,commit.getParent(0).getTree());
                        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                        newTreeIter.reset(reader,commit.getTree());

                        List<DiffEntry> entries = git.diff()
                                .setOldTree(oldTreeIter)
                                .setNewTree(newTreeIter)
                                .call();

                        for (DiffEntry entry : entries){
                            System.out.println(entry.getNewPath());
                        }
                    }*/

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoHeadException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    private static void getDiff(RevCommit newCommit) throws IOException, GitAPIException {
        RevCommit prevCommit = newCommit.getParent(0);
        if (prevCommit == null){
            System.out.println("start of repo");
        }
        AbstractTreeIterator oldTree = getCanonicalTreeParser(prevCommit);
        AbstractTreeIterator newTree = getCanonicalTreeParser(newCommit);
        OutputStream outputStream = new ByteArrayOutputStream();
        DiffFormatter diffFormatter = new DiffFormatter(outputStream);
        diffFormatter.setRepository(repo);
        diffFormatter.format(oldTree,newTree);

        System.out.println(outputStream);


    }

    private static AbstractTreeIterator getCanonicalTreeParser(RevCommit commit) throws IOException {
        ObjectReader reader = git.getRepository().newObjectReader();
        return new CanonicalTreeParser(null,reader,commit.getTree());

    }


}
