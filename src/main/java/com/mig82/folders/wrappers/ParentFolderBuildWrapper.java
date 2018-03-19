package com.mig82.folders.wrappers;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.mig82.folders.Messages;
import com.mig82.folders.properties.FolderProperties;
import com.mig82.folders.properties.StringProperty;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A build wrapper which exposes the properties defined for a folder to all the jobs contained inside it.
 * Freestyle jobs must opt into using this build wrapper and pipeline jobs can access these properties by using the
 * custom {@code withFolderProperties} step.
 *
 * @author Miguelangel Fernandez Mendoza
 */
public class ParentFolderBuildWrapper extends SimpleBuildWrapper{

	private static final Logger LOGGER = Logger.getLogger( ParentFolderBuildWrapper.class.getName() );
	private FolderProperties folderProperties;

	@DataBoundConstructor
	public ParentFolderBuildWrapper(){}

	//Add the properties from the parent ProjectFolder folder to the context of the job before it starts.
	@Override
	public void setUp(
		Context context,
		Run<?, ?> run,
		FilePath workspace,
		Launcher launcher,
		TaskListener listener,
		EnvVars initialEnvironment
	) throws IOException, InterruptedException {

		Job job = run.getParent(); //The parent of the run is the Job itself.
		loadFolderProperties(job, context);

		LOGGER.log(Level.FINE, "7. Context env is: {0}", context.getEnv().toString());
	}

	public void loadFolderProperties(Job job, Context context){

		LOGGER.log(Level.FINER, "1. Searching for folder properties in ancestors of: {0}\n", job.getDisplayName());

		ItemGroup parent = job.getParent();

		//Look in all the ancestors...
		while (parent != null) {

			if (parent instanceof AbstractFolder) {

				LOGGER.log(Level.FINEST, "2. Searching for folder properties in: {0}\n", parent.getDisplayName());

				AbstractFolder folder = (AbstractFolder) parent;
				folderProperties = (FolderProperties) folder.getProperties().get(FolderProperties.class);
				if (folderProperties != null) {

					StringProperty[] newlyFoundProperties = folderProperties.getProperties();
					LOGGER.log(Level.FINER, "3. Found {0} folder properties in {1}\n", new Object[]{
						newlyFoundProperties.length,
						parent.getDisplayName()
					});

					//If we find folder project properties on this parent, we add all to the context.
					for(StringProperty property: newlyFoundProperties){
						//Only add the property if it has not been already defined in a sub-folder.
						if(context.getEnv().get(property.getKey()) == null){
							LOGGER.log(Level.FINEST, "4. Adding ({0}, {1}) to the context env", new Object[]{
								property.getKey(),
								property.getValue()
							});
							context.env(property.getKey(), property.getValue());
						}
						else{
							LOGGER.log(Level.FINEST, "4. Will not add duplicate property {0} to the context env", new Object[]{
								property.getKey()
							});
						}
					}
					LOGGER.log(Level.FINEST, "5. Context env: {0}", context.getEnv().toString());
				}
			}
			else if(parent instanceof Jenkins){
				LOGGER.log(Level.FINEST, "2. Reached Jenkins root. Stopping search\n");
			}
			else{
				LOGGER.log(Level.WARNING, "2. Unknown parent type: {0} of class {1}\n", new Object[]{
					parent.getDisplayName(),
					parent.getClass().getName()
				});
			}

			//In the next iteration we want to search for the parent of this parent.
			if (parent instanceof Item) {
				parent = ((Item) parent).getParent();
			}
			else {
				parent = null;
			}
		}
		LOGGER.log(Level.FINE, "6. Context env is: {0}", context.getEnv().toString());
	}

	@Symbol("withFolderProperties")
	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public String getDisplayName() {
			return Messages.display_build_wrapper();
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {

			LOGGER.log(Level.FINER, "Folder build wrapper is applicable to: {0}\n", item.getDisplayName());
			return true;
		}
	}
}