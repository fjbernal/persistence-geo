/*
 * FoldersAdminServiceImpl.java
 * 
 * Copyright (C) 2011
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
 * Authors:: Alejandro Díaz Torres (mailto:adiaz@emergya.com)
 */
package com.emergya.persistenceGeo.service.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.emergya.persistenceGeo.dao.AbstractGenericDao;
import com.emergya.persistenceGeo.dao.AuthorityEntityDao;
import com.emergya.persistenceGeo.dao.FolderEntityDao;
import com.emergya.persistenceGeo.dao.FolderTypeEntityDao;
import com.emergya.persistenceGeo.dao.GenericDAO;
import com.emergya.persistenceGeo.dao.LayerEntityDao;
import com.emergya.persistenceGeo.dao.UserEntityDao;
import com.emergya.persistenceGeo.dao.ZoneEntityDao;
import com.emergya.persistenceGeo.dto.FolderDto;
import com.emergya.persistenceGeo.dto.FolderTypeDto;
import com.emergya.persistenceGeo.metaModel.AbstractFolderEntity;
import com.emergya.persistenceGeo.metaModel.AbstractFolderTypeEntity;
import com.emergya.persistenceGeo.metaModel.AbstractLayerEntity;
import com.emergya.persistenceGeo.metaModel.AbstractUserEntity;
import com.emergya.persistenceGeo.metaModel.Instancer;
import com.emergya.persistenceGeo.service.FoldersAdminService;
import com.emergya.persistenceGeo.service.LayerAdminService;
import org.apache.commons.lang3.BooleanUtils;

/**
 * FoldersAdminService transactional implementation based on daos uses
 * {@link AbstractGenericDao}
 * 
 * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
 * 
 */
@Repository
@Transactional
public class FoldersAdminServiceImpl extends
		AbstractServiceImpl<FolderDto, AbstractFolderEntity> implements
		FoldersAdminService {

	@Resource
	private Instancer instancer;
	@Resource
	private FolderEntityDao folderDao;
	@Resource
	private LayerEntityDao layerDao;
	@Resource
	private UserEntityDao userDao;
	@Resource
	private AuthorityEntityDao authDao;
	@Resource
	private ZoneEntityDao zoneDao;
	@Resource
	private FolderTypeEntityDao folderTypeDao;

	@Resource
	private LayerAdminService layerAdminService;

	public FoldersAdminServiceImpl() {
		super();
	}

	@Override
	@Cacheable("persistenceGeo")
	public FolderDto getRootFolder(Long idUser) {
		return entityToDto(folderDao.findRootByUser(idUser));
	}

	@Override
	@Cacheable("persistenceGeo")
	public FolderDto getRootGroupFolder(Long idGroup) {
		return entityToDto(folderDao.findRootByGroup(idGroup));
	}

	/**
	 * Saves a folder
	 * 
	 * @return saved folder
	 */
        @Override
	public FolderDto saveFolder(FolderDto folder) {
		AbstractFolderEntity entity = dtoToEntity(folder);
		return entityToDto(folderDao.makePersistent(entity));
	}

	/**
	 * Remove children and layers before remove folder
	 */
	@Override
	public void delete(Serializable dto) {
		FolderDto folder = (FolderDto) dto;

		// Cascade remove
		if (folder.getFolderList() != null) {
			// children remove
			for (FolderDto child : folder.getFolderList()) {
				delete(child);
			}
		}

		// Remove layers
		List<AbstractLayerEntity> layers = layerDao.getLayersByFolder(folder
				.getId());
		if (layers != null) {			
                    for (AbstractLayerEntity layer : layers) {
                            layerDao.makeTransient(layer);
                    }
		}

		super.delete(dto);
	}

	/**
	 * Copy userContext from an user <code>origin</code> to a user
	 * <code>target</code>
	 * 
	 * @param originUserId
	 *            origin user's <code>id</code>
	 * @param targetUserId
	 *            target user's <code>id</code>
	 * @param merge
	 *            indicate if target user folders must be maintained
	 */
	@Transactional
        @Override
	public FolderDto copyUserContext(Long originUserId, Long targetUserId,
			boolean merge) {
		FolderDto toCopy = getRootFolder(originUserId);
		if (!merge) {
			deleteUserContext(targetUserId);
		}
		// copy root folder
		return copyFolder(targetUserId, toCopy);
	}

	/**
	 * Delete all user folders and layers
	 * 
	 * @param userId
	 *            user's <code>id</code>
	 */
        @Override
	public void deleteUserContext(Long userId) {
		FolderDto rootFolder = getRootFolder(userId);
		while (rootFolder != null) {
			delete(rootFolder);
			rootFolder = getRootFolder(userId);
		}
	}

	/**
	 * Copy folder to an user
	 * 
	 * @param targetUserId
	 * @param originFolder
	 * 
	 * @return copied
	 */
        @Override
	public FolderDto copyFolder(Long targetUserId, FolderDto originFolder) {
		return copyFolder(targetUserId, originFolder, null);
	}

	/**
	 * Copy folder to an user
	 * 
	 * @param targetUserId
	 * @param originFolder
	 * @param idParent
	 * 
	 * @return copied
	 */
	public FolderDto copyFolder(Long targetUserId, FolderDto originFolder,
			Long idParent) {
		try {
			FolderDto result = (FolderDto) originFolder.clone();
			result.setIdUser(targetUserId);
			result.setIdAuth(null);
			result.setIdParent(idParent);
			result = saveFolder(result);

			// Cascade copy
			if (originFolder.getFolderList() != null) {
				List<FolderDto> children = new LinkedList<FolderDto>();
				// children copy
				for (FolderDto child : originFolder.getFolderList()) {
					children.add(copyFolder(targetUserId, child, result.getId()));
				}
				result.setFolderList(children);
			}

			// Copy layers
			List<AbstractLayerEntity> layers = layerDao
					.getLayersByFolder(originFolder.getId());
			if (layers != null) {
				AbstractUserEntity user = userDao.findById(targetUserId, false);
				AbstractFolderEntity folder = folderDao.findById(
						result.getId(), false);
				
                                // layers copy
                                for (AbstractLayerEntity layer : layers) {
                                        AbstractLayerEntity clonedLayer = (AbstractLayerEntity) layer
                                                        .clone();
                                        clonedLayer.setUser(user);
                                        clonedLayer.setFolder(folder);
                                        layerDao.save(clonedLayer);
                                }
			}

			return entityToDto(folderDao.findById(result.getId(), false));

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get all channel folders filterd
	 * 
	 * @param inZone
	 *            indicates if obtain channel folders with a zone. If this
	 *            parameter is null only obtain not zoned channels
	 * @param idZone
	 *            filter by zone. Obtain only channels of the zone identified by
	 *            <code>idZone</code>
	 * 
	 * @return folder list
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<FolderDto> getChannelFolders(Boolean inZone, Long idZone) {
		return (List<FolderDto>) entitiesToDtos(folderDao.getChannelFolders(
				inZone, idZone));
	}

	/**
	 * Get a folders list by zones. If zoneId is NULL returns all the folder not
	 * associated to any zone.
	 * 
	 * @params <code>zoneId</code>
	 * 
	 * @return Entities list associated with the zoneId or null if not found
	 */
        @Override
	public List<FolderDto> findByZone(Long zoneId) {
		List<FolderDto> foldersDto = new LinkedList<FolderDto>();
		List<AbstractFolderEntity> folders = folderDao.findByZone(zoneId);
		for (AbstractFolderEntity folderEntity : folders) {
			foldersDto.add(entityToDto(folderEntity));
		}
		return foldersDto;
	}

	/**
	 * Get a folders list by zones with an specific parent. If zoneId is NULL
	 * returns all the folder not associated to any zone. If parentId is NULL
	 * the returned folders are root folders.
	 * 
	 * @params <code>zoneId</code>
	 * @params <code>parentId</code>
	 * 
	 * @return Entities list associated with the zoneId or null if not found
	 */
        @Override
	public List<FolderDto> findByZone(Long zoneId, Long parentId) {
		List<FolderDto> foldersDto = new LinkedList<FolderDto>();
		List<AbstractFolderEntity> folders = folderDao.findByZone(zoneId,
				parentId);
		for (AbstractFolderEntity folderEntity : folders) {
			foldersDto.add(entityToDto(folderEntity));
		}
		return foldersDto;
	}

	/**
	 * Get all channel folders filtered
	 * 
	 * @param inZone
	 *            indicates if obtain channel folders with a zone. If this
	 *            parameter is null only obtain not zoned channels
	 * @param idZone
	 *            filter by zone. Obtain only channels of the zone identified by
	 *            <code>idZone</code>
	 * @param isEnabled
	 * 
	 * @return folder list
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<FolderDto> getChannelFolders(Boolean inZone, Long idZone,
			Boolean isEnabled) {
		return (List<FolderDto>) entitiesToDtos(folderDao.getChannelFolders(
				inZone, idZone, isEnabled));
	}
	
	/**
	 * Get all channel folders filterd
	 * 
	 * @param inZone indicates if obtain channel folders with a zone. If this parameter is null only obtain not zoned channels
	 * @param idZone filter by zone. Obtain only channels of the zone identified by <code>idZone</code>
	 * @param isEnabled
	 * @param folderType folder type to obtain
	 * 
	 * @return folder list
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<FolderDto> getChannelFolders(Boolean inZone, Long idZone, Boolean isEnabled, Long folderType){
		return (List<FolderDto>) entitiesToDtos(folderDao.getChannelFolders(
				inZone, idZone, isEnabled, folderType));
	}

	/**
	 * Get a folders list by zones. If zoneId is NULL returns all the folder not
	 * associated to any zone.
	 * 
	 * @param <code>zoneId</code>
	 * @param isEnabled
	 * 
	 * @return Entities list associated with the zoneId or null if not found
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<FolderDto> findByZone(Long zoneId, Boolean isEnabled) {
		return (List<FolderDto>) entitiesToDtos(folderDao.findByZone(zoneId,
				isEnabled));
	}

	/**
	 * Get a folders list by zones with an specific parent. If zoneId is NULL
	 * returns all the folder not associated to any zone. If parentId is NULL
	 * the returned folders are root folders.
	 * 
	 * @param <code>zoneId</code>
	 * @param <code>parentId</code>
	 * @param isEnabled
	 * 
	 * @return Entities list associated with the zoneId or null if not found
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<FolderDto> findByZone(Long zoneId, Long parentId,
			Boolean isEnabled) {
		return (List<FolderDto>) entitiesToDtos(folderDao.findByZone(zoneId,
				parentId, isEnabled));
	}

        @Override
	protected FolderDto entityToDto(AbstractFolderEntity entity) {
		FolderDto dto = null;
		if (entity != null) {
			dto = new FolderDto();
			dto.setEnabled(entity.getEnabled());
			dto.setIsChannel(entity.getIsChannel());
			dto.setUpdateDate(entity.getUpdateDate());
			dto.setCreateDate(entity.getCreateDate());
			dto.setId(entity.getId());
			dto.setName(entity.getName());
			dto.setOrder(entity.getFolderOrder());

			// Children
			List<AbstractFolderEntity> children = folderDao.getFolders(entity
					.getId());
			if (children != null && !children.isEmpty()) {
				dto.setIsChannel(false);
				List<FolderDto> subFolders = new LinkedList<FolderDto>();
				for (AbstractFolderEntity child : children) {
					// Recursive case
					subFolders.add(entityToDto(child));
				}
				dto.setFolderList(subFolders);
			} else {
				// only is channel if have layers
				List<AbstractLayerEntity> layers = layerDao
						.getLayersByFolder(entity.getId());
				if (layers != null && !layers.isEmpty()) {
					dto.setIsChannel(true);
				} else {
					if (entity.getIsChannel() != null && entity.getIsChannel()) {
						dto.setIsChannel(entity.getIsChannel());
					} else {
						dto.setIsChannel(false);
					}
				}
			}

			// Parent
			if (entity.getParent() != null
					&& entity.getParent().getId() != null) {
				dto.setIdParent(entity.getParent().getId());
			}

			// Auth
			if (entity.getAuthority() != null
					&& entity.getAuthority().getId() != null) {
				dto.setIdAuth(entity.getAuthority().getId());
			}

			// User
			if (entity.getUser() != null && entity.getUser().getId() != null) {
				dto.setIdUser((Long) entity.getUser().getId());
			}

			// Zone
			if (entity.getZone() != null && entity.getZone().getId() != null) {
				dto.setZoneId((Long) entity.getZone().getId());
			}

			// Folder Type
			if (entity.getFolderType() != null
					&& entity.getFolderType().getId() != null) {
				dto.setIdFolderType(entity.getFolderType().getId());
			}
		}
		return dto;
	}

        @Override
	protected AbstractFolderEntity dtoToEntity(FolderDto dto) {
		AbstractFolderEntity entity = null;
		if (dto != null) {
			if (dto.getId() != null) {
				entity = folderDao.findById(dto.getId(), false);
			} else {
				entity = instancer.createFolder();
			}
			entity.setEnabled(dto.getEnabled());
			entity.setIsChannel(dto.getIsChannel());
			entity.setUpdateDate(dto.getUpdateDate());
			entity.setCreateDate(dto.getCreateDate());
			entity.setName(dto.getName());
			entity.setFolderOrder(dto.getOrder());

			// TODO: Children if is necesary

			// Parent
			if (dto.getIdParent() != null) {
				AbstractFolderEntity parent = folderDao.findById(
						dto.getIdParent(), false);
				entity.setParent(parent);
			}

			// Auth & user
			if (dto.getIdAuth() != null) {
				entity.setAuthority(authDao.findById(dto.getIdAuth(), false));
			}
			if (dto.getIdUser() != null) {
				entity.setUser(userDao.findById(dto.getIdUser(), false));
			}

			// Zone
			if (dto.getZoneId() != null) {
				entity.setZone(zoneDao.findById(dto.getZoneId(), false));
			}

			// Folder Type
			if (dto.getIdFolderType() != null) {
				entity.setFolderType(folderTypeDao.findById(
						dto.getIdFolderType(), false));
			}
		}
		return entity;
	}

	@Override
	protected GenericDAO<AbstractFolderEntity, Long> getDao() {
		return folderDao;
	}

	/**
	 * @return List<FolderTypeDto> Devuelve la lista de todos los folder types
	 */
        @Override
	public List<FolderTypeDto> getAllFolderType() {
		List<FolderTypeDto> dtoList = new LinkedList<FolderTypeDto>();
		List<AbstractFolderTypeEntity> entityList = folderTypeDao.findAll();
		if (entityList != null) {
			for (AbstractFolderTypeEntity e : entityList) {
				dtoList.add(entityToDto(e));
			}
		}
		return dtoList;
	}

	/**
	 * @param <code>excluded</code>
	 * 
	 * @return List<FolderTypeDto> Devuelve la lista de todos los folder types
	 *         excluyendo los del excluded
	 */
        @Override
	public List<FolderTypeDto> getIPTtFolderType(String[] excluded) {
		List<FolderTypeDto> dtoList = new LinkedList<FolderTypeDto>();
		List<AbstractFolderTypeEntity> entityList = folderTypeDao.findAll();
		int[] indexToRemove = new int[excluded.length];
		if (entityList != null) {
			for (int i = 0; i < entityList.size(); i++) {
				for (int j = 0; j < excluded.length; j++) {
					if (excluded[j].equals(entityList.get(i).getType())) {
						indexToRemove[j] = i;
					}
				}
			}
			for (int el = 0; el < indexToRemove.length; el++) {
				entityList.remove(indexToRemove[el]);
			}
			for (AbstractFolderTypeEntity e : entityList) {
				dtoList.add(entityToDto(e));
			}
		}
		return dtoList;
	}

	/**
	 * Convierte de FolderTypeEntity a FolderTypeDto
	 * 
	 * @param user
	 * @return
	 */
	protected FolderTypeDto entityToDto(AbstractFolderTypeEntity folderType) {
		FolderTypeDto dto = null;
		if (folderType != null) {
			dto = new FolderTypeDto();
			dto.setId(folderType.getId());
			dto.setType(folderType.getType());
			dto.setTitle(folderType.getTitle());
			dto.setParent(folderType.getParent().getId());
		}
		return dto;
	}

        @Override
	public List<FolderDto> findFoldersByType(Long typeId) {
		List<FolderDto> dtoList = new LinkedList<FolderDto>();
		List<AbstractFolderEntity> dtoTemp;
		if (typeId != null) {
			dtoTemp = folderDao.findByType(typeId);
			for (AbstractFolderEntity f : dtoTemp) {
				dtoList.add(entityToDto(f));
			}
		}
		return dtoList;
	}

	/**
	 * @return List<FolderTypeDto> folder types without children
	 */
        @Override
	public List<FolderTypeDto> getNotParentFolderTypes() {
		List<FolderTypeDto> dtoList = new LinkedList<FolderTypeDto>();
		List<AbstractFolderTypeEntity> entityList = folderTypeDao
				.getNotParentFolderTypes();
		if (entityList != null) {
			for (AbstractFolderTypeEntity e : entityList) {
				dtoList.add(entityToDto(e));
			}
		}
		return dtoList;
	}

	@Override
	public List<FolderTypeDto> getFolderTypes(Long parentId) {
		List<FolderTypeDto> dtoList = new LinkedList<FolderTypeDto>();
		List<AbstractFolderTypeEntity> entityList = folderTypeDao
				.getFolderTypes(parentId);
		if (entityList != null) {
			for (AbstractFolderTypeEntity e : entityList) {
				dtoList.add(entityToDto(e));
			}
		}
		return dtoList;
	}
	
	/**
	 * @param <code>typeId</code>
	 * 
	 * @return List<FolderTypeDto>
	 * 			Devuelve la lista de todos los folder types que tenga el mismo type id y no tengan padre
	 */
        @Override
	public List<FolderDto> rootFoldersByType(Long typeId){

		List<FolderDto> dtoList = new LinkedList<FolderDto>();
		List<AbstractFolderEntity> dtoTemp;
		if (typeId != null) {
			dtoTemp = folderDao.rootFoldersByType(typeId);
			for (AbstractFolderEntity f : dtoTemp) {
                                if(BooleanUtils.isTrue(f.getEnabled())) {
                                    dtoList.add(entityToDto(f));
                                }
			}
		}
		return dtoList;
	}

	/**
	 * Returns all root folders.
	 * @return 
	 */
	@Override
	public List<FolderDto> rootFolders() {
	    List<FolderDto> dtoList = new LinkedList<FolderDto>();
	    
	    List<AbstractFolderEntity> entList = folderDao.rootFoldersByType(null);
	    for (AbstractFolderEntity f : entList) {
		    if(BooleanUtils.isTrue(f.getEnabled())) {
			dtoList.add(entityToDto(f));
		    }
	    }
	    
	    return dtoList;
	}
}
