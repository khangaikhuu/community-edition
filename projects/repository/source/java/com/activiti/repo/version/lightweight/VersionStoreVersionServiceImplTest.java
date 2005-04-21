package com.activiti.repo.version.lightweight;

import java.util.Collection;

import com.activiti.repo.ref.NodeRef;
import com.activiti.repo.version.Version;
import com.activiti.repo.version.VersionHistory;

/**
 * LightWeightVersionService test class.
 * 
 * @author Roy Wetherall
 */
public class VersionStoreVersionServiceImplTest extends VersionStoreBaseImplTest
{
	/**
     * Tests the creation of the initial version of a versionable node
     */
    public void testCreateIntialVersion()
    {
        NodeRef versionableNode = createNewVersionableNode();
        createVersion(versionableNode);
    }     
    
    /**
     * Test creating a version history with many versions from the same workspace
     */
    public void testCreateManyVersionsSameWorkspace()
    {
        NodeRef versionableNode = createNewVersionableNode();
        createVersion(versionableNode);
        // TODO mess with some of the properties and stuff as you version
        createVersion(versionableNode);
        // TODO mess with some of the properties and stuff as you version
        createVersion(versionableNode);
    }
    
    // TODO test versioning a non versionable node
    
    // TODO test versioning numberious times with branchs implies by different workspaces
    
    /**
     * Test versioning the children of a verionable node
     */
    public void testVersioningChildren()
    {
        NodeRef versionableNode = createNewVersionableNode();
        
        // Snap shot data
        int expectedVersionNumber = peekNextVersionNumber(); 
        long beforeVersionTime = System.currentTimeMillis();
        
        // Version the node and its children
        Collection<Version> versions = this.lightWeightVersionStoreVersionService.createVersion(
                versionableNode, 
                this.versionProperties,
                true);
        
        // Check the returned versions are correct
        CheckVersionCollection(expectedVersionNumber, beforeVersionTime, versions);
        
        // TODO check the version history is correct
    }
    
    /**
     * Test versioning many nodes in one go
     */
    public void testVersioningManyNodes()
    {
        createNewVersionableNode();
        
        // Snap shot data
        int expectedVersionNumber = peekNextVersionNumber(); 
        long beforeVersionTime = System.currentTimeMillis();
        
        // Version the list of nodes created
        Collection<Version> versions = this.lightWeightVersionStoreVersionService.createVersion(
                this.versionableNodes.values(),
                this.versionProperties);
        
        // Check the returned versions are correct
        CheckVersionCollection(expectedVersionNumber, beforeVersionTime, versions);     

        // TODO check the version histories
    }
    
    /**
     * Helper method to check the validity of the list of newly created versions.
     * 
     * @param expectedVersionNumber  the expected version number that all the versions should have
     * @param beforeVersionTime      the time before the versions where created
     * @param versions               the collection of version objects
     */
    private void CheckVersionCollection(int expectedVersionNumber, long beforeVersionTime, Collection<Version> versions)
    {
        for (Version version : versions)
        {
            // Get the frozen id from the version
            String frozenNodeId = (String)version.getVersionProperty(Version.PROP_FROZEN_NODE_ID);
            assertNotNull("Unable to retrieve the frozen node id from the created version.", frozenNodeId);
            
            // Get the origional node ref (based on the forzen node)
            NodeRef origionaNodeRef = this.versionableNodes.get(frozenNodeId);
            assertNotNull("The versionable node ref that relates to the frozen node id can not be found.", origionaNodeRef);
            
            // Check the new version
            checkNewVersion(beforeVersionTime, expectedVersionNumber, version, origionaNodeRef);
        }
    }
    
    /**
     * Tests the version history
     */
    public void testNoVersionHistory()
    {
        NodeRef nodeRef = createNewVersionableNode();
        
        VersionHistory vh = this.lightWeightVersionStoreVersionService.getVersionHistory(nodeRef);
        assertNull(vh);
    }
    
    /**
     * Tests getVersionHistory when all the entries in the version history
     * are from the same workspace.
     */
    public void testGetVersionHistorySameWorkspace()
    {
        NodeRef versionableNode = createNewVersionableNode();
        
        Version version1 = addToVersionHistory(versionableNode, null);
        Version version2 = addToVersionHistory(versionableNode, version1);
        Version version3 = addToVersionHistory(versionableNode, version2);
        Version version4 = addToVersionHistory(versionableNode, version3);
        addToVersionHistory(versionableNode, version4);        
    }
    
    /**
     * Adds another version to the version history then checks that getVersionHistory is returning
     * the correct data.
     * 
     * @param versionableNode  the versionable node reference
     * @param parentVersion    the parent version
     */
    private Version addToVersionHistory(NodeRef versionableNode, Version parentVersion)
    {
        Version createdVersion = createVersion(versionableNode);
        
        VersionHistory vh = this.lightWeightVersionStoreVersionService.getVersionHistory(versionableNode);
        assertNotNull("The version history should not be null since we know we have versioned this node.", vh);
        
        if (parentVersion == null)
        {
            // Check the root is the newly created version
            Version root = vh.getRootVersion();
            assertNotNull(
                    "The root version should never be null, since every version history ust have a root version.", 
                    root);
            assertEquals(createdVersion.getVersionLabel(), root.getVersionLabel());
        }
        
        // Get the version from the version history
        Version version = vh.getVersion(createdVersion.getVersionLabel());
        assertNotNull(version);
        assertEquals(createdVersion.getVersionLabel(), version.getVersionLabel());
        
        // Check that the version is a leaf node of the version history (since it is newly created)
        Collection<Version> suc = vh.getSuccessors(version);
        assertNotNull(suc);
        assertEquals(0, suc.size());
        
        // Check that the predessor is the passed parent version (if root version should be null)
        Version pre = vh.getPredecessor(version);
        if (parentVersion == null)
        {
            assertNull(pre);
        }
        else
        {
            assertNotNull(pre);
            assertEquals(parentVersion.getVersionLabel(), pre.getVersionLabel());
        }
        
        if (parentVersion != null)
        {
            // Check that the successors of the parent are the created version
            Collection<Version> parentSuc = vh.getSuccessors(parentVersion);
            assertNotNull(parentSuc);
            assertEquals(1, parentSuc.size());
            Version tempVersion = (Version)parentSuc.toArray()[0];
            assertEquals(version.getVersionLabel(), tempVersion.getVersionLabel());
        }
        
        return createdVersion;
    }
}
