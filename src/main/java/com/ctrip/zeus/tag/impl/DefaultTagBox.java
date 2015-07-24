package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.tag.TagBox;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/16.
 */
@Component("tagBox")
public class DefaultTagBox implements TagBox {
    @Resource
    private TagDao tagDao;
    @Resource
    private TagItemDao tagItemDao;

    @Override
    public List<String> getAllTags() throws Exception {
        List<String> result = new ArrayList<>();
        for (TagDo tagDo : tagDao.findAll(TagEntity.READSET_FULL)) {
            result.add(tagDo.getName());
        }
        return result;
    }

    @Override
    public void removeTag(String name) throws Exception {
        TagDo d = tagDao.findByName(name, TagEntity.READSET_FULL);
        if (d == null)
            return;
        tagDao.delete(d);
        tagItemDao.deleteTag(new TagItemDo().setTagId(d.getId()));
    }

    @Override
    public void renameTag(String oldName, String newName) throws Exception {
        TagDo d = tagDao.findByName(oldName, TagEntity.READSET_FULL);
        if (d == null)
            return;
        tagDao.update(d.setName(newName), TagEntity.UPDATESET_FULL);
    }

    @Override
    public void tagging(String tagName, String type, Long[] itemIds) throws Exception {
        TagDo d = tagDao.findByName(tagName, TagEntity.READSET_FULL);
        if (d == null) {
            d = new TagDo().setName(tagName);
            tagDao.insert(d);
        }
        TagItemDo[] l = new TagItemDo[itemIds.length];
        for (int i = 0; i < itemIds.length; i++) {
            l[i] = new TagItemDo().setTagId(d.getId()).setType(type).setItemId(itemIds[i]);
        }
        tagItemDao.insert(l);
    }

    @Override
    public void untagging(String tagName, String type, Long[] itemIds) throws Exception {
        TagDo d = tagDao.findByName(tagName, TagEntity.READSET_FULL);
        if (d == null) {
            throw new ValidationException("Tag named " + tagName + "is not found.");
        }
        if (itemIds != null) {
            TagItemDo[] l = new TagItemDo[itemIds.length];
            for (int i = 0; i < itemIds.length; i++) {
                l[i] = new TagItemDo().setTagId(d.getId()).setType(type).setItemId(itemIds[i]);
            }
            tagItemDao.deleteTagItems(l);
        } else {
            tagItemDao.deleteTagType(new TagItemDo().setTagId(d.getId()).setType(type));
        }
    }
}
