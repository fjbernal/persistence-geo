/*
 * AbstractEntity.java
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
 */
package com.emergya.persistenceGeo.metaModel;

import java.io.Serializable;

/**
 * Entity from which extend the rest of the entities
 * 
 * @author <a href="mailto:adiaz@emergya.es">adiaz</a>
 */
@SuppressWarnings("serial")
public abstract class AbstractEntity implements Serializable{

	/**
	 * @return the id
	 */
	public abstract Serializable getId();

	/**
	 * @param id the id to set
	 */
	public abstract void setId(Serializable id);

	/**
	 * If both entities have an id, it's compare;
	 * otherwise compare entity objects
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * @return true if entities have equals id's or are the same object
	 */
	public boolean equals(Object obj) {
		AbstractEntity another = (AbstractEntity) obj;
		if(getId() != null){
			return getId().equals(another.getId());
		}else{
			return super.equals(obj);
		}
	}
	
}
