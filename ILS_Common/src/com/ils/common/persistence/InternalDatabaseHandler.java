/**
 *   (c) 2019  ILS Automation. All rights reserved. 
 */
package com.ils.common.persistence;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.common.project.GlobalProps;
import com.inductiveautomation.ignition.common.project.ProjectNotFoundException;
import com.inductiveautomation.ignition.common.project.RuntimeProject;
import com.inductiveautomation.ignition.common.user.AuthenticatedUser;
import com.inductiveautomation.ignition.common.user.BasicAuthenticatedUser;
import com.inductiveautomation.ignition.gateway.IgnitionGateway;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.project.ProjectManager;

/**
 *  Use this class to edit information in the internal Ignition database.
 *  It is usable only in a Gateway context. 
 */
public class InternalDatabaseHandler {
	private final static String CLSS = "InternalDatabaseHandler";
	
	private final ILSLogger log;
	private GatewayContext context = null;
    
	/**
	 * Constructor:
	 */
	public InternalDatabaseHandler() { 
		log = LogMaker.getLogger(getClass().getPackage().getName());
		context = IgnitionGateway.get();
	}
	
	/**
	 * We don't really know which project we want, so we guess. Change any 
	 * projects with a default provider equal to the old datasource and
	 * update to the new. We shouldn't be using default datasources 
	 * anyway.
	 * @param name
	 */
	public void setProjectDatasource(String newName) {
		ProjectManager pmgr = context.getProjectManager();
		List<String> names = pmgr.getProjectNames();
		Properties admin = getAdministrativeUser();
		try {
			if( admin!=null ) {
				for( String name: names ) {
					GlobalProps props = pmgr.getProjectProps(name);
					log.infof("%s.setProviderDatasource: Found project %s, datasource %s=%s?",CLSS,name,props.getDefaultDatasourceName(),newName);
					if( !props.getDefaultDatasourceName().equalsIgnoreCase(newName)) {
						props.setDefaultDatasourceName(newName);
						AuthenticatedUser user = new BasicAuthenticatedUser(props.getAuthProfileName(),admin.getProperty("ProfileId"),
								admin.getProperty("Name", "admin"),props.getRequiredRoles());
						try {
							Optional<RuntimeProject> optional = pmgr.getProject(name);
							RuntimeProject rp = optional.get();  // should always exist
							pmgr.createOrReplaceProject(name,rp.getManifest(),rp.getResources());
							log.infof("%s.setProviderDatasource: Saved project %s, datasource now %s",CLSS,name,newName);
						}
						// We get an error notifying project listeners. It appears not to matter with us.
						catch(Exception ex) {
							log.errorf("%s.setProjectDatasource: Exception when saving merged project (%s)",CLSS,ex.getLocalizedMessage());
						}
					}
				}
			}
			else {
				log.errorf("%s.setProjectDatasource: No admin user found to save project",CLSS);
			}
		}
		catch( ProjectNotFoundException pnfe) {
			log.errorf("%s.setProjectDatasource: Coding error (%s)",CLSS,pnfe.getLocalizedMessage());
		}
	}
	/**
	 * Create an alarm notification profile entry with the given name, but only if it does not already exist.
	 * @param name
	 */
	public void setProviderDatasource(String name) {
		PersistenceSession session = null;
		String SQL = "";

		try {
			String providerName = "default";  // Hard-coded
			session = context.getPersistenceInterface().getSession();
			Connection cxn = session.getJdbcConnection();
			long datasourceId = getDatasourceId(cxn,name);
			long providerId   = getProviderId(cxn,providerName);
			if( datasourceId>=0 ) {
				SQL = "UPDATE InternalSQLTProviderSettings SET defaultDatasourceId = ? "
						+ " WHERE profileId = ?" ;

				PreparedStatement statement = cxn.prepareStatement(SQL);
				statement.setLong(1, datasourceId);
				statement.setLong(2, providerId);
				statement.executeUpdate();
				statement.close();
			}
			else {
				log.warnf("%s.setProviderDatasource: Datasource not found (%s)",CLSS,name);
			}
		}
		catch(SQLException sqle) {
			log.warn("\n"+SQL+"\n");
			log.warnf("%s.setProviderDatasource: Exception (%s)",CLSS,sqle.getMessage());
		}
		finally {
			if(session!=null) session.close();
		}
	}
	
	/**
	 * Search the internal database for a user in the specified profile that has admin
	 * privileges. This is the user that we want to make the owner of our projects.
	 * @return properties for the first admin user found. Keys are: "Name", "ProfileId"
	 */
	public Properties getAdministrativeUser() {
		Properties props = null;
		try {
			PersistenceSession session = context.getPersistenceInterface().getSession();
			Connection cxn = session.getJdbcConnection();
			String SQL = 
					"SELECT USER.UserName,ROLE.profileId " +
					"FROM  InternalUserTable USER, InternalAuthMappingTable MAP,InternalRoleTable ROLE " +
					"WHERE ROLE.roleName = 'Administrator' " +
					"  AND ROLE.roleId = MAP.roleId" +
					"  AND MAP.userId = USER.userId";
			//log.info("\n"+SQL+"\n");
			Statement statement = cxn.createStatement();
			ResultSet rs = statement.executeQuery(SQL);
			if (rs.next()) {
				props = new Properties();
				props.setProperty("Name", rs.getString(1));
				props.setProperty("ProfileId", rs.getString(2));
				log.infof("%s.getAdminstrativeUsers: %s (%s)",CLSS,rs.getString(1),rs.getString(2));
			}
			rs.close();
			statement.close();
			session.close();
		}
		catch(SQLException sqle) {
			log.warnf("%s.getAdministrativeUsers: Exception finding admin users (%s)",CLSS,sqle.getMessage());
		}
		return props;
	}
	/**
	 * @param connection a SQLite connection
	 * @return the id associated with the currently configured datasource
	 */
	private long getDatasourceId(Connection cxn,String name) {
		long id = -1;
		String SQL = "";
		try {
			SQL = "SELECT DS.datasources_id FROM datasources DS"
				+ " WHERE DS.name = ?";
			PreparedStatement statement = cxn.prepareStatement(SQL);
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			if( !rs.isAfterLast() ) {
				rs.next();
				id = rs.getLong(1);   // 1-based
			}
			else {
				log.warn(String.format("%s.getDatasourceId: No datasource %s found",CLSS,name));
			}
			rs.close();
			statement.close();
		}
		catch(SQLException sqle) {
			log.warn(String.format("\n%s\n",SQL));
			log.warn(String.format("%s.getDatasourceId: Exception (%s)",CLSS,sqle.getMessage()),sqle);
		}
		return id;
	}

	/**
	 * @param connection a SQLite connection
	 * @return the id associated with the currently configured datasource
	 */
	private long getProviderId(Connection cxn,String name) {
		long id = -1;
		String SQL = "";
		try {
			SQL = "SELECT sqltagprovider_id FROM SQLTagProvider DS"
				+ " WHERE Name = ?";
			PreparedStatement statement = cxn.prepareStatement(SQL);
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			if(!rs.isAfterLast() ) {
				rs.next();
				id = rs.getLong(1);   // 1-based
			}
			else {
				log.warn(String.format("%s.getProviderId: No tag provider %s found",CLSS,name));
			}
			rs.close();
			statement.close();
		}
		catch(SQLException sqle) {
			log.warn(String.format("\n%s\n",SQL));
			log.warn(String.format("%s.getProviderId: Exception (%s)",CLSS,sqle.getMessage()),sqle);
		}
		return id;
	}
}

