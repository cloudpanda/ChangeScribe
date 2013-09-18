package co.edu.unal.colswe.CommitSummarizer.core.summarizer;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jgit.api.Git;

import co.edu.unal.colswe.CommitSummarizer.core.ast.ProjectInformation;
import co.edu.unal.colswe.CommitSummarizer.core.git.ChangedFile;
import co.edu.unal.colswe.CommitSummarizer.core.git.ChangedFile.TypeChange;
import co.edu.unal.colswe.CommitSummarizer.core.stereotype.stereotyped.StereotypeIdentifier;
import co.edu.unal.colswe.CommitSummarizer.core.stereotype.stereotyped.StereotypedElement;
import co.edu.unal.colswe.CommitSummarizer.core.textgenerator.phrase.MethodPhraseGenerator;
import co.edu.unal.colswe.CommitSummarizer.core.util.Utils;

public class SummarizeChanges {
	
	private Git git;
	private StereotypeIdentifier stereotypeIdentifier;
	private StringBuilder comment = new StringBuilder();
	
	public SummarizeChanges(Git git) {
		super();
		this.git = git;
		this.stereotypeIdentifier = new StereotypeIdentifier();
	}

	@SuppressWarnings("unused")
	public void summarize(ChangedFile[] differences) {
		for (ChangedFile file : differences) {
			try {
				if(!file.getChangeType().equals(TypeChange.UNTRACKED.name())) {
					File left = Utils.getFileContentOfLastCommit(file.getPath(), getGit().getRepository());
					File right = new File(file.getAbsolutePath());
					
					/*if(file.getAbsolutePath().endsWith(".java")) {
						distiller.extractClassifiedSourceCodeChanges(left, right);
						
						List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
						if(changes != null) {
						    for(SourceCodeChange change : changes) {
						    	System.out.println(cd.generateChangeDescription(change));
						    }
						}
						
					}*/
				} else {
					MethodPhraseGenerator phraseGenerator = new MethodPhraseGenerator();
					if(file.getAbsolutePath().endsWith(".java")) {
						String projectName = ProjectInformation.getProject(ProjectInformation.getSelectedProject()).getName();
						IResource res = ProjectInformation.getProject(ProjectInformation.getSelectedProject()).findMember(file.getPath().replaceFirst(projectName, ""));
						IFile ifile = ProjectInformation.getSelectedProject().getWorkspace().getRoot().getFile(res.getFullPath());
						stereotypeIdentifier = new StereotypeIdentifier((ICompilationUnit) JavaCore.create(ifile), 0, 0);
						stereotypeIdentifier.identifyStereotypes();
						
						
						for (StereotypedElement element : stereotypeIdentifier.getStereotypedElements()) {
							System.out.println("Class: " + element.getName().toString() + " - Stereotype: " + element.getStereotypes());
							String classDescription = "The " + element.getStereotypes() + " class " + element.getName().toString() + " was added. This class allows: \n";
							getComment().append(classDescription);
							for (StereotypedElement method : element.getStereoSubElements()) {
								//System.out.println("Method: " + method.getQualifiedName() + " - Stereotype: " + method.getStereotypes());
								String description = phraseGenerator.generate(method.getQualifiedName(), "BASIC", method, element);
								if(description != null && !description.equals("")) {
									getComment().append(description);
								}
							}
							getComment().append("\n");
						}
					} else {
						//TODO other files
					}
				}
			} catch(Exception e) {
			    System.err.println("Warning: error while change distilling. " + e.getMessage());
			}
			
		}
	}
	
	

	public Git getGit() {
		return git;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public StringBuilder getComment() {
		return comment;
	}

	public void setComment(StringBuilder comment) {
		this.comment = comment;
	}

}