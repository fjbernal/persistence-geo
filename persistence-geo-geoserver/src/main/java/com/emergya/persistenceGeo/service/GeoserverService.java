/* GeoserverService.java
 * 
 * Copyright (C) 2012
 * 
 * This file is part of project persistence-geo-core
 * 
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 * 
 * Authors:: Juan Luis Rodríguez Ponce (mailto:jlrodriguez@emergya.com)
 */
package com.emergya.persistenceGeo.service;

import it.geosolutions.geoserver.rest.decoder.RESTDataStore;

import java.io.File;
import java.util.List;

import com.emergya.persistenceGeo.utils.BoundingBox;
import com.emergya.persistenceGeo.utils.GsCoverageDetails;
import com.emergya.persistenceGeo.utils.GsCoverageStoreData;
import com.emergya.persistenceGeo.utils.GeometryType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;

/**
 * @author <a href="mailto:jlrodriguez@emergya.com">jlrodriguez</a>
 * 
 */
public interface GeoserverService {
	public static final String VECTORIAL_LAYER_TYPE = "postgis";
	public static final String GEOTIFF_LAYER_TYPE_NAME = "geotiff";
	public static final String IMAGEWORLD_LAYER_TYPE_NAME = "imageworld";
	public static final String IMAGEMOSAIC_LAYER_TYPE_NAME = "imagemosaic";
	public static final String WFS_LAYER_TYPE_NAME = "WFS";

	public final String DEFAULT_SRS = "EPSG:4326";

	public boolean createGsWorkspaceWithDatastore(String workspaceName);

	public boolean deleteGsWorkspace(String workspaceName);

	public boolean unpublishGsDbLayer(String workspaceName, String layer);

	/**
	 * Unpublish layer identified by layerName, workspace and workspace
	 * 
	 * @param workspaceName
	 * @param datastoreName
	 * @param layerName
	 * 
	 * @return true if can be unpublish and false otherwise
	 */
	public boolean unpublishLayer(String workspaceName, String datastoreName, String layerName);
	
	public void copyLayer(String workspaceName, String datastoreName,
			String layerName, String tableName, String title, BoundingBox bbox,
			GeometryType type, String targetWorkspaceName,
			String targetDatastoreName, String targetLayerName);

	/**
	 * Publishes a vectorial layer with data in a PostGis database with the
	 * default styles and given parameters.
	 * 
	 * @param workspaceName
	 * @param table_name
	 * @param layerName
	 * @param title
	 * @param nativeBoundingBox
	 * @param type
	 * @return
	 */
	boolean publishGsDbLayer(String workspaceName, String table_name,
			String layerName, String title, BoundingBox nativeBoundingBox,
			GeometryType type);

	/**
	 * Check if a layer with <code>layerName</code> exists in the workspace
	 * <code>workspaceName</code>
	 * 
	 * @param layerName
	 *            layer name.
	 * @param workspaceName
	 *            workspace name.
	 * @return <code>true</code> if a layer named <code>layerName</code> exists
	 *         in the workspace named <code>workspaceName</code>.
	 */
	public boolean existsLayerInWorkspace(String layerName, String workspaceName);

	/**
	 * Create a Postgis datastore using a JNDI connection.
	 * 
	 * @param workspaceName
	 *            the workspace where datastore will be created.
	 * @param datastoreName
	 *            the name of the new datastore.
	 * @return
	 */
	public boolean createDatastoreJndi(String workspaceName,
			String datastoreName);

	/**
	 * Upload and publish a GeoTIFF image.
	 * 
	 * @param workspace
	 *            workspace to use.
	 * @param layerName
	 *            the layer name to be created.
	 * @param geotiff
	 *            the GeoTIFF file.
	 * @param crs
	 *            the image native SRS.
	 * @return <code>true</code> if success.
	 */
	public boolean publishGeoTIFF(String workspace, String layerName,
			File geotiff, String crs);

	/**
	 * Upload and publish an Image Mosaic image.
	 * 
	 * @param workspaceName
	 *            workspace to use.
	 * @param layerName
	 *            the store name to be created. It will also be the created 
	 *            coverage store
	 * @param imageFile
	 *            a ZIP file with an image mosaic.
	 * @param crs
	 *            the image mosaic coordinates system.
	 * @return <code>true</code> if success.
	 */
	public boolean publishImageMosaic(String workspaceName, String layerName,
			File imageFile, String crs);

	/**
	 * Upload and publish an World Image file.
	 * 
	 * @param workspaceName
	 *            workspace to use.
	 * @param layerName
	 *            the layer name to be created.
	 * @param imageFile
	 *            a ZIP file with an World Image.
	 * @param crs
	 *            the world image coordinates system.
	 * @return <code>true</code> if success.
	 */
	public boolean publishWorldImage(String workspaceName, String layerName,
			File imageFile, String crs);

	/**
	 * Gets info about a coverage store.
	 * 
	 * @param workspaceName
	 * @param coverageStoreName
	 * @return
	 */
	public GsCoverageStoreData getCoverageStoreData(String workspaceName,
			String coverageStoreName);

	/**
	 * Unpublishes a layer stored in a coverage store from the geoserver. Also,
	 * it deletes the
	 * 
	 * @param workspaceName
	 * @param layerName
	 * @return
	 */
	public boolean unpublishGsCoverageLayer(String workspaceName,
			String layerName);

	/**
	 * Retrieves the details of a coverage store: bbox, projection, etc.
	 * 
	 * @param workspaceName
	 * @param coverageStore
	 * @param coverageName
	 * @return
	 */
	public GsCoverageDetails getCoverageDetails(String workspaceName,
			String coverageStore, String coverageName);

	/**
	 * Copies the style of the source layer with a new name, including the sdl
	 * file stored in the server.
	 * 
	 * @param sourceLayerName
	 * @param newLayerName
	 * @return
	 */
	public boolean copyLayerStyle(String sourceLayerName, String newLayerName);

	/**
	 * Changes a layer style. The style must exist in geoserver.
	 * 
	 * @param workspaceName
	 * @param layerName
	 * @param newStyleName
	 * @return
	 */
	public boolean setLayerStyle(String workspaceName, String layerName,
			String newStyleName);

	/**
	 * Removes an style from GeoServer.
     * @param styleName
     * @return 
	 */
	public boolean deleteStyle(String styleName);
	
	public boolean reset();
	/**
	 * Checks if a workspace exists on the server.
	 * 
	 * @param workspaceName
	 *            name to check.
	 * @return <code>true</code> if the workspace exists, <code>false</code> if
	 *         not.
	 */
	public boolean existsWorkspace(String workspaceName);
	
	/**
	 * Retrieves a layer's datastore associated with the layer.
	 * 
	 * @param layerName
	 * 
	 * @return
	 */
	public RESTDataStore getDatastore(String layerName);
	
	/**
	 * Retrieves the geoserver url configured.
	 * 
	 * @return baseUrl to geoserver
	 */
	public String getGeoserverUrl();
	
	/**
	 * Retrieves all layers' names in geoserver
	 * 
	 * @return layers' names
	 */
	public List<String> getLayersNames();

	/**
	 * Obtain native name of a layerName 
	 * 
	 * @param layerName
	 * 
	 * @return native name of the layer 
	 */
	public String getNativeName(String layerName);
	
	/**
	 * Retrieves all styles' names in geoserver
	 * 
	 * @return styles' names
	 */
	public List<String> getStyleNames();

	
	/**
	 * Clean styles unused in style list 
	 * 
	 * @param styleNames name of styles to be deleted
	 * 
	 * @return list of deleted styles
	 */
	public List<String> cleanUnusedStyles(List<String> styleNames);

	/**
	 * Gets the workspace a layer is in.
	 * given its name.
	 * @param layerName
	 * @return 
	 */
	public String getLayerWorkspace(String layerName);
	
	/**
	 * Gets Geoserver's info on the layer given its name.
	 * @param layerName
	 * @return 
	 */
        public RESTLayer getLayerInfo(String layerName);
	
	
	/**
	 * Enumerate type containing result codes for the <c>duplicateGeoServerLayer</c>
	 * method.
	 */
	public enum DuplicationResult {
	    SUCCESS_VECTORIAL,
	    SUCCESS_RASTER,
	    SUCCESS_REMOTE,
	    FAILURE
	}

	/**
	 * Duplicates an existing layer in geoserver.
	 * 
	 * In the future, the source layer's type should be detected automatically.
	 * 
	 * @param sourceWorkspace The source layer's workspace.
	 * @param sourceLayerType The source layer's type description. The new layer's type will be the same.
	 * @param sourceLayerName The source layer's name in geoserver.
	 * @param sourceLayerTable The source layer's table backing its data (only needed if the source layer's type is postgis).
	 * @param targetWorkspace The workspace the new layer will be created in.
	 * @param newLayerName The new layer's geoserver name.
	 * @param newLayerTitle The new layer's title.
	 * @return 
	 */
	public DuplicationResult duplicateGeoServerLayer(
		String sourceWorkspace,
		String sourceLayerType,
		String sourceLayerName,
		String sourceLayerTable,
		String targetWorkspace,
		String newLayerName,
		String newLayerTitle);

	/**
	 * Removes a layer from geoserver, deleting also its backing data.
	 * 
	 * In the future, we should try to detect the layer's geoserver type automatically.
	 * 
	 * @param workspace The workspace the layer exists in.
	 * @param layerName The name of the layer in geoserver.
	 * @param layerType The layer type's description
	 * @param tableName The layer's table name (only needed if its a postgis layer)
	 * @return 
	 */
	public boolean deleteGeoServerLayer(String workspace, String layerName, String layerType, String tableName);
}
