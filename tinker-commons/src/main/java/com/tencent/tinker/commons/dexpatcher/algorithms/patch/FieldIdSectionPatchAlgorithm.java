/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tinker.commons.dexpatcher.algorithms.patch;

import com.tencent.tinker.android.dex.Dex;
import com.tencent.tinker.android.dex.FieldId;
import com.tencent.tinker.android.dex.TableOfContents;
import com.tencent.tinker.android.dex.io.DexDataBuffer;
import com.tencent.tinker.android.dx.util.IndexMap;
import com.tencent.tinker.commons.dexpatcher.struct.DexPatchFile;
import com.tencent.tinker.commons.dexpatcher.struct.SmallPatchedDexItemFile;

/**
 * Created by tangyinsheng on 2016/7/4.
 */
public class FieldIdSectionPatchAlgorithm extends DexSectionPatchAlgorithm<FieldId> {
    private TableOfContents.Section patchedFieldIdTocSec = null;
    private Dex.Section patchedFieldIdSec = null;

    public FieldIdSectionPatchAlgorithm(
            DexPatchFile patchFile,
            Dex oldDex,
            Dex patchedDex,
            IndexMap oldToFullPatchedIndexMap,
            IndexMap fullPatchedToSmallPatchedIndexMap,
            final SmallPatchedDexItemFile extraInfoFile
    ) {
        this(
                patchFile,
                oldDex,
                patchedDex,
                oldToFullPatchedIndexMap,
                fullPatchedToSmallPatchedIndexMap,
                new SmallPatchedDexItemChooser() {
                    @Override
                    public boolean isPatchedItemInSmallPatchedDex(
                            String oldDexSign, int patchedItemIndex
                    ) {
                        return extraInfoFile.isFieldIdInSmallPatchedDex(
                                oldDexSign, patchedItemIndex
                        );
                    }
                }
        );
    }

    public FieldIdSectionPatchAlgorithm(
            DexPatchFile patchFile,
            Dex oldDex,
            Dex patchedDex,
            IndexMap oldToFullPatchedIndexMap,
            IndexMap fullPatchedToSmallPatchedIndexMap,
            SmallPatchedDexItemChooser spdItemChooser
    ) {
        super(
                patchFile,
                oldDex,
                oldToFullPatchedIndexMap,
                fullPatchedToSmallPatchedIndexMap,
                spdItemChooser
        );

        if (patchedDex != null) {
            this.patchedFieldIdTocSec = patchedDex.getTableOfContents().fieldIds;
            this.patchedFieldIdSec = patchedDex.openSection(this.patchedFieldIdTocSec);
        }
    }

    @Override
    protected TableOfContents.Section getTocSection(Dex dex) {
        return dex.getTableOfContents().fieldIds;
    }

    @Override
    protected FieldId nextItem(DexDataBuffer section) {
        return section.readFieldId();
    }

    @Override
    protected int getItemSize(FieldId item) {
        return item.byteCountInDex();
    }

    @Override
    protected int getFullPatchSectionBase() {
        if (this.patchFile != null) {
            return this.patchFile.getPatchedFieldIdSectionOffset();
        } else {
            return getTocSection(this.oldDex).off;
        }
    }

    @Override
    protected FieldId adjustItem(IndexMap indexMap, FieldId item) {
        return indexMap.adjust(item);
    }

    @Override
    protected int writePatchedItem(FieldId patchedItem) {
        ++this.patchedFieldIdTocSec.size;
        return this.patchedFieldIdSec.writeFieldId(patchedItem);
    }

    @Override
    protected void updateIndexOrOffset(IndexMap indexMap, int oldIndex, int oldOffset, int newIndex, int newOffset) {
        if (oldIndex != newIndex) {
            indexMap.mapFieldIds(oldIndex, newIndex);
        }
    }

    @Override
    protected void markDeletedIndexOrOffset(IndexMap indexMap, int deletedIndex, int deletedOffset) {
        indexMap.markFieldIdDeleted(deletedIndex);
    }
}
