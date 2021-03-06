/*
 * PermissionEntityDao.java
 * 
 * Copyright (C) 2012
 * 
 * This file is part of Proyecto persistenceGeo
 * 
 * This software is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * As a special exception, if you link this library with other files to produce
 * an executable, this library does not by itself cause the resulting executable
 * to be covered by the GNU General Public License. This exception does not
 * however invalidate any other reasons why the executable file might be covered
 * by the GNU General Public License.
 * 
 * Authors:: Moisés Arcos Santiago (mailto:marcos@emergya.com)
 */
package com.emergya.persistenceGeo.dao;

import java.util.List;

import com.emergya.persistenceGeo.metaModel.AbstractPermissionEntity;

/**
 * DAO that represents the permission
 * 
 * @author <a href="mailto:marcos@emergya.com">marcos</a>
 *
 */
public interface PermissionEntityDao extends GenericDAO<AbstractPermissionEntity, Long> {

	/**
	 * Create a new permission in the system
	 * 
	 * @param <code>permission</code>
	 * 
	 * @return Entity from the created permission
	 */
	public AbstractPermissionEntity createPermission(String permission);
	
	/**
	 * Get a permissions list by the permission name 
	 * 
	 * @param <code>permissionName</code>
	 * 
	 * @return Entities list associated with the permission name or null if not found 
	 */
	public List<AbstractPermissionEntity> getPermissions(String permissionName);
	
	/**
	 * Delete a permission by the permission identifier 
	 * 
	 * @param <code>permissionID</code>
	 * 
	 */
	public void deletePermission(Long permissionID);
	
	/**
	 * Get a permissions list by authority 
	 * 
	 * @param <code>authorithyId</code> if this parameter is null search permissions without authority type
	 * 
	 * @return Entities list associated with the authorithyId name or null if not found 
	 */
	public List<AbstractPermissionEntity> getPermissionsByAuthorithy(Long authorithyId);
	
	/**
	 * Get a permissions list by authority type 
	 * 
	 * @param <code>authorithyTypeId</code> 
	 * 
	 * @return Entities list associated with the authorithyTypeId name or null if not found 
	 */
	public List<AbstractPermissionEntity> getPermissionsByAuthorithyType(Long authorithyTypeId);
}
