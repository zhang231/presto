/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator;

import com.facebook.presto.spi.block.RandomAccessBlock;
import com.facebook.presto.spi.block.SortOrder;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.facebook.presto.operator.SyntheticAddress.decodePosition;
import static com.facebook.presto.operator.SyntheticAddress.decodeSliceIndex;
import static com.google.common.base.Preconditions.checkNotNull;

public class SimplePagesIndexComparator
        implements PagesIndexComparator
{
    private final List<Integer> sortChannels;
    private final List<SortOrder> sortOrders;

    public SimplePagesIndexComparator(List<Integer> sortChannels, List<SortOrder> sortOrders)
    {
        this.sortChannels = ImmutableList.copyOf(checkNotNull(sortChannels, "sortChannels is null"));
        this.sortOrders = ImmutableList.copyOf(checkNotNull(sortOrders, "sortOrders is null"));
    }

    @Override
    public int compareTo(PagesIndex pagesIndex, int leftPosition, int rightPosition)
    {
        long leftPageAddress = pagesIndex.getValueAddresses().getLong(leftPosition);
        int leftBlockIndex = decodeSliceIndex(leftPageAddress);
        int leftBlockPosition = decodePosition(leftPageAddress);

        long rightPageAddress = pagesIndex.getValueAddresses().getLong(rightPosition);
        int rightBlockIndex = decodeSliceIndex(rightPageAddress);
        int rightBlockPosition = decodePosition(rightPageAddress);

        for (int i = 0; i < sortChannels.size(); i++) {
            int sortChannel = sortChannels.get(i);
            RandomAccessBlock leftBlock = pagesIndex.getChannel(sortChannel).get(leftBlockIndex);
            RandomAccessBlock rightBlock = pagesIndex.getChannel(sortChannel).get(rightBlockIndex);

            int compare = leftBlock.compareTo(sortOrders.get(i), leftBlockPosition, rightBlock, rightBlockPosition);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }
}
