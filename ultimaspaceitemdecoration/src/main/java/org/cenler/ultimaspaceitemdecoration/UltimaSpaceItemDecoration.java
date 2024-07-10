package org.cenler.ultimaspaceitemdecoration;

import static androidx.annotation.Dimension.DP;
import static androidx.recyclerview.widget.StaggeredGridLayoutManager.HORIZONTAL;
import static androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams;
import static androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL;
import static java.lang.Math.max;
import static java.lang.Math.round;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: RecycleView.ItemDecoration 分割线&间距
 * <p>
 * 1、适配线性布局、网格布局以及流式布局
 * 2、网格布局分割线推荐使用背景+间距实现
 * 通过 {@link DividerBuilder} 创建实例
 */
public final class UltimaSpaceItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * TAG
     */
//    static final String TAG = "UltimaSpaceItemDecoration";

    private final Context mContext;
    private SpanInfo mSpanInfo;
    private final Paint mPaint;
    private final DividerBuilder mBuilder;

    private UltimaSpaceItemDecoration(Context context, DividerBuilder dividerBuilder) {
        mContext = context;
        mBuilder = dividerBuilder;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mContext.getColor(mBuilder.colorRes == 0 ? android.R.color.transparent : mBuilder.colorRes));
        mPaint.setStrokeWidth(mBuilder.dividerSize);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View targetChild = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(targetChild);
            if (filter(position, parent)) {
                continue;
            }

            final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                // 网格布局：建议使用间距+背景颜色方式实现
                final int orientation = ((GridLayoutManager) layoutManager).getOrientation();
                drawGridLayoutBoundRect(c, orientation, parent, targetChild);
            } else if (layoutManager instanceof LinearLayoutManager) {
                // 线性布局
                final int orientation = ((LinearLayoutManager) layoutManager).getOrientation();
                drawLinearLayoutBoundRect(c, orientation, parent, targetChild);
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                // 瀑布流布局：保留未实现（建议使用间距+容器背景颜色方式实现）
                final int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                drawStaggeredGridBoundRect(c, orientation, parent, targetChild);
            }
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }

        final int position = parent.getChildAdapterPosition(view);
        if (filter(position, parent)) {
            return;
        }

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // 修正分割线范围
        mBuilder.verticalSpace = Math.max(mBuilder.verticalSpace, mBuilder.dividerSize);
        mBuilder.horizontalSpace = Math.max(mBuilder.horizontalSpace, mBuilder.dividerSize);

        if (layoutManager instanceof GridLayoutManager) {

            calculateGridLayoutPadding(outRect, view, parent, state);
        } else if (layoutManager instanceof LinearLayoutManager) {

            calculateLinearLayoutPadding(outRect, view, parent, state);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {

            calculateStaggeredGridLayoutPadding(outRect, view, parent, state);
        }
    }

    /**
     * @param position 当前Item
     * @param parent   RecycleView
     * @return 是否过滤
     */
    private boolean filter(int position, RecyclerView parent) {
        if (mBuilder.visibilityProvider.filter(position, parent)) {
            return true;
        }

        if (mBuilder.skipStart > position) {
            return true;
        }

        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter != null && mBuilder.endSkip >= adapter.getItemCount() - position) {
            return true;
        }

        return false;
    }

    /**
     * Description: 计算间距及偏移
     *
     * @param outRect child view rect 空间
     * @param view    child view
     * @param parent  RecycleView
     * @param state   RecyclerView.State
     */
    private void calculateLinearLayoutPadding(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();

        final Rect targetRect = new Rect();
        final int itemCount = parent.getAdapter().getItemCount();
        final int orientation = layoutManager.getOrientation();

        final int childAdapterPosition = parent.getChildAdapterPosition(view);
        final boolean isFirstDivider = mBuilder.skipStart == childAdapterPosition;
        final boolean isLastDivider = mBuilder.endSkip + childAdapterPosition == (itemCount - 1);

        if (orientation == RecyclerView.VERTICAL) {

            targetRect.left = mBuilder.paddingStart;
            targetRect.right = mBuilder.paddingEnd;
            if (isFirstDivider && isLastDivider) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.top = mBuilder.paddingTop;
                }
                if (!mBuilder.isHiddenLast) {
                    targetRect.bottom = mBuilder.paddingBottom;
                }
            } else if (isFirstDivider) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.top = mBuilder.paddingTop;
                }
                targetRect.bottom = mBuilder.verticalSpace;
            } else if (isLastDivider) {
                if (!mBuilder.isHiddenLast) {
                    targetRect.bottom = mBuilder.paddingBottom;
                }
            } else {
                targetRect.bottom = mBuilder.verticalSpace;
            }

            if (isReverseLayout(parent)) {
                outRect.set(targetRect.left, targetRect.bottom, targetRect.right, targetRect.top);
            } else {
                outRect.set(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom);
            }
        } else if (orientation == RecyclerView.HORIZONTAL) {

            targetRect.top = mBuilder.paddingTop;
            targetRect.bottom = mBuilder.paddingBottom;
            if (isFirstDivider && isLastDivider) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.left = mBuilder.paddingStart;
                }
                if (!mBuilder.isHiddenLast) {
                    targetRect.right = mBuilder.paddingEnd;
                }
            } else if (isFirstDivider) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.left = mBuilder.paddingStart;
                }
                targetRect.right = mBuilder.horizontalSpace;
            } else if (isLastDivider) {
                if (!mBuilder.isHiddenLast) {
                    targetRect.right = mBuilder.paddingEnd;
                }
            } else {
                targetRect.right = mBuilder.horizontalSpace;
            }

            if (isReverseLayout(parent)) {
                outRect.set(targetRect.right, targetRect.top, targetRect.left, targetRect.bottom);
            } else {
                outRect.set(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom);
            }
        }
    }

    /**
     * Description: 绘制网格布局分割线
     *
     * @param outRect child view rect 空间
     * @param view    child view
     * @param parent  RecycleView
     * @param state   RecyclerView.State
     */
    private void calculateGridLayoutPadding(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();

        final Rect targetRect = new Rect();
        final int itemCount = parent.getAdapter().getItemCount();
        final int position = parent.getChildAdapterPosition(view);

        final GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
        final int spanCount = layoutManager.getSpanCount();
        final int currSpanSize = spanSizeLookup.getSpanSize(position);

        final boolean isSingleSpan = currSpanSize == spanCount;

        if (mSpanInfo == null) {
            mSpanInfo = new SpanInfo(parent, layoutManager);
            parent.getAdapter().registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mSpanInfo = new SpanInfo(parent, layoutManager);
                }
            });
        }

        final int firstRow = mSpanInfo.getRowByPosition(mBuilder.skipStart);
        final int lastRow = mSpanInfo.getRowByPosition(itemCount - mBuilder.endSkip - 1);

        boolean isFirstRow = mSpanInfo.isInRow(position, firstRow);
        boolean isLastRow = mSpanInfo.isInRow(position, lastRow);
        boolean isFirstSpan = mSpanInfo.isFirstSpan(position);
        boolean isLastSpan = mSpanInfo.isLastSpan(position);

        // Log.d(TAG, String.format("P(%d): FD-%b LD-%b isSingleSpan-%b firstSpan-%b lastSpan-%b"
        //        , position, isFirstDivider, isLastDivider, isSingleSpan, isFirstSpan, isLastSpan));

        // 32 32 32 = 96 / 4 = 24
        // 24 8 24 8 24 24

        final int orientation = layoutManager.getOrientation();
        if (orientation == LinearLayoutManager.VERTICAL) {
            int relativeIndex = mSpanInfo.getCurrentRowRelativeIndex(position);
            int relativeItemCount = mSpanInfo.getCurrentRowRelativeItemCount(position);

            // 根据是否显示外部的间距来计算总的间距个数
            final int spaceNum = getSpaceNum(spanCount, mBuilder.horizontalOuter);
            // 间距均值
            final float eachSpace = getTotalSpace(spaceNum, orientation) * 1.0f / spanCount;

            int offsetLeft = round(relativeIndex % spanCount * (mBuilder.horizontalSpace - eachSpace) + getHorizontalOuterSpace(true));
            int offsetRight = round(eachSpace - offsetLeft);

            if (relativeItemCount != spanCount) {
                if (isFirstSpan) {
                    relativeIndex = 0;
                    offsetLeft = getHorizontalOuterSpace(true);
                    offsetRight = round(eachSpace - offsetLeft);
                } else if (isLastSpan) {
                    relativeIndex = spanCount - 1;
                    offsetLeft = round(relativeIndex % spanCount * (mBuilder.horizontalSpace - eachSpace) + getHorizontalOuterSpace(true));
                    offsetRight = round(eachSpace - offsetLeft);
                }

                int offsetIndex = mSpanInfo.getCurrentRowRelativeOffsetIndex(position);
                if (offsetIndex != relativeIndex) {
                    int tempLeftOffset = round(offsetIndex % spanCount * (mBuilder.horizontalSpace - eachSpace) + getHorizontalOuterSpace(true));
                    offsetRight = round(eachSpace - tempLeftOffset);
                }
            }

            // eachSpace(24) 32
            // Position      0    1   2   3   4   5  6   7   8   9   10  11  12
            // OffsetLeft    0    8   16  24  0   8  16  8   16  8
            // OffsetRight   24   16   8  0   24  16 8   16  8   16
            if (isFirstRow && isLastRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.top = getVerticalOuterSpace(true);
                }
                if (!mBuilder.isHiddenLast) {
                    targetRect.bottom = getVerticalOuterSpace(false);
                }
            } else if (isFirstRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.top = getVerticalOuterSpace(true);
                }
                targetRect.bottom = mBuilder.verticalSpace;
            } else if (isLastRow) {
                if (!mBuilder.isHiddenLast) {
                    targetRect.bottom = getVerticalOuterSpace(false);
                }
            } else {
                targetRect.bottom = mBuilder.verticalSpace;
            }

            if (isSingleSpan) {
                targetRect.left = getHorizontalOuterSpace(true);
                targetRect.right = getHorizontalOuterSpace(false);
            } else if (isFirstSpan) {
                targetRect.left = offsetLeft;
                targetRect.right = offsetRight;
            } else if (isLastSpan) {
                targetRect.left = offsetLeft;
                targetRect.right = offsetRight;
            } else {
                targetRect.left = offsetLeft;
                targetRect.right = offsetRight;
            }

            if (isReverseLayout(parent)) {
                outRect.set(targetRect.left, targetRect.bottom, targetRect.right, targetRect.top);
            } else {
                outRect.set(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom);
            }
        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            int relativeIndex = mSpanInfo.getCurrentRowRelativeIndex(position);
            int relativeItemCount = mSpanInfo.getCurrentRowRelativeItemCount(position);

            // 根据是否显示外部的间距来计算总的间距个数
            final int spaceNum = getSpaceNum(spanCount, mBuilder.verticalOuter);
            // 均值偏移量
            final float eachSpace = getTotalSpace(spaceNum, orientation) * 1.0f / spanCount;

            int offsetTop = round(relativeIndex % spanCount * (mBuilder.verticalSpace - eachSpace)) + getVerticalOuterSpace(true);
            int offsetBottom = round(eachSpace - offsetTop);

//            Log.d("P:%d - T&B(%d, %d)", position, offsetTop, offsetBottom);

            if (relativeItemCount != spanCount) {
                if (isFirstSpan) {
                    relativeIndex = 0;
                    offsetTop = getVerticalOuterSpace(true);
                    offsetBottom = round(eachSpace - offsetTop);
                } else if (isLastSpan) {
                    relativeIndex = spanCount - 1;
                    offsetTop = round(relativeIndex % spanCount * (mBuilder.verticalSpace - eachSpace) + getVerticalOuterSpace(true));
                    offsetBottom = round(eachSpace - offsetTop);
                }

                // 一个Item占多喝Span情况需要修正
                int offsetIndex = mSpanInfo.getCurrentRowRelativeOffsetIndex(position);
                if (offsetIndex != relativeIndex) {
                    int tempLeftOffset = round(offsetIndex % spanCount * (mBuilder.verticalSpace - eachSpace) + getVerticalOuterSpace(true));
                    offsetBottom = round(eachSpace - tempLeftOffset);
                } else {
                    offsetTop = round(offsetIndex % spanCount * (mBuilder.verticalSpace - eachSpace) + getVerticalOuterSpace(true));
                }
            }

            if (isFirstRow && isLastRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.left = getHorizontalOuterSpace(true);
                }
                if (!mBuilder.isHiddenLast) {
                    targetRect.right = getHorizontalOuterSpace(false);
                }
            } else if (isFirstRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.left = getHorizontalOuterSpace(true);
                }
                targetRect.right = mBuilder.horizontalSpace;
            } else if (isLastRow) {
                if (!mBuilder.isHiddenLast) {
                    targetRect.right = getHorizontalOuterSpace(false);
                }
            } else {
                targetRect.right = mBuilder.horizontalSpace;
            }

            if (isSingleSpan) {
                targetRect.top = getVerticalOuterSpace(true);
                targetRect.bottom = getVerticalOuterSpace(false);
            } else if (isFirstSpan) {
                targetRect.top = offsetTop;
                targetRect.bottom = offsetBottom;
            } else if (isLastSpan) {
                targetRect.top = offsetTop;
                targetRect.bottom = offsetBottom;
            } else {
                targetRect.top = offsetTop;
                targetRect.bottom = offsetBottom;
            }

            if (isReverseLayout(parent)) {
                outRect.set(targetRect.right, targetRect.top, targetRect.left, targetRect.bottom);
            } else {
                outRect.set(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom);
            }
        }
    }

    /**
     * Description: 当前行或列的总间距空间
     *
     * @param spaceNum    间距的个数
     * @param orientation 布局方向
     * @return 当前行或列的总间距空间
     */
    private int getTotalSpace(int spaceNum, int orientation) {
        if (orientation == RecyclerView.HORIZONTAL) {
            int totalSpace = spaceNum * mBuilder.verticalSpace;
            if (!mBuilder.verticalOuter) {
                totalSpace += mBuilder.paddingTop + mBuilder.paddingBottom;
            }
            return totalSpace;
        } else {
            int totalSpace = spaceNum * mBuilder.horizontalSpace;
            if (!mBuilder.horizontalOuter) {
                totalSpace += mBuilder.paddingStart + mBuilder.paddingEnd;
            }
            return totalSpace;
        }
    }

    /**
     * Description: 间距的数量，显示外边距 n+1，否则 n-1
     *
     * @param spanCount   span 数量
     * @param isShowOuter 是否显示外边距或分割线
     * @return 间距的数量，显示外边距 n+1，否则 n-1
     */
    private int getSpaceNum(int spanCount, boolean isShowOuter) {
        return spanCount + (isShowOuter ? 1 : -1);
    }

    /**
     * Description: 获取横向的外边距
     *
     * @param isLeft 是否显示左边距，否则右边距
     * @return 获取横向的外边距
     */
    private int getHorizontalOuterSpace(boolean isLeft) {
        if (mBuilder.horizontalOuter) {
            return Math.max(mBuilder.horizontalSpace, mBuilder.dividerSize);
        }
        if (isLeft) {
            return mBuilder.paddingStart;
        } else {
            return mBuilder.paddingEnd;
        }
    }

    /**
     * 绘制网格布局分割线
     *
     * @param isTop 是否边距，否则底边距
     * @return 获取垂直方向外边距
     */
    private int getVerticalOuterSpace(boolean isTop) {
        if (mBuilder.verticalOuter) {
            return Math.max(mBuilder.verticalSpace, mBuilder.dividerSize);
        }
        if (isTop) {
            return mBuilder.paddingTop;
        } else {
            return mBuilder.paddingBottom;
        }
    }

    /**
     * 计算流式布局 View rect 空间范围
     *
     * @param outRect child view rect 空间
     * @param view    child view
     * @param parent  RecycleView
     * @param state   RecyclerView.State
     */
    private void calculateStaggeredGridLayoutPadding(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();

        final Rect targetRect = new Rect();
        final int itemCount = parent.getAdapter().getItemCount();
        final int position = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();

        final LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();

        final int spanIndex = layoutParams.getSpanIndex();
        final boolean isFirstRow = position - mBuilder.skipStart < spanCount;
        final boolean isLastRow = position + mBuilder.endSkip == itemCount - 1;
        final boolean isFirstSpan = (spanIndex % spanCount) == 0;
        final boolean isLastSpan = (spanIndex % spanCount) == spanCount - 1;
        final boolean isSingleSpan = layoutParams.isFullSpan();

//        Log.d(TAG, String.format(Locale.getDefault(),
//                "P(%d-%d): FR-%b LR-%b firstSpan-%b lastSpan-%b",
//                position, spanIndex, isFirstRow, isLastRow, isFirstSpan, isLastSpan));

        final int orientation = layoutManager.getOrientation();
        if (orientation == VERTICAL) {
            // 均值偏移量
            final int eachSpace = round((spanCount - 1) * mBuilder.horizontalSpace * 1.0f / spanCount);
            final int offsetLeft = round(spanIndex % spanCount * (mBuilder.horizontalSpace - eachSpace));
            final int offsetRight = round(eachSpace - offsetLeft);

            if (!isSingleSpan) {
                targetRect.left = offsetLeft;
                targetRect.right = offsetRight;
            }
            if (isFirstRow && isLastRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.top = mBuilder.paddingTop;
                }
                if (!mBuilder.isHiddenLast) {
                    targetRect.bottom = mBuilder.paddingBottom;
                }
            } else if (isFirstRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.top = mBuilder.paddingTop;
                }
                targetRect.bottom = mBuilder.verticalSpace;
            } else if (isLastRow) {
                if (!mBuilder.isHiddenLast) {
                    targetRect.bottom = mBuilder.paddingBottom;
                }
            } else {
                targetRect.bottom = mBuilder.verticalSpace;
            }
        } else if (orientation == HORIZONTAL) {
            // 均值偏移量
            final int eachSpace = round((spanCount - 1) * mBuilder.verticalSpace * 1.0f / spanCount);
            final int offsetTop = round(spanIndex % spanCount * (mBuilder.verticalSpace - eachSpace));
            final int offsetBottom = round(eachSpace - offsetTop);

            if (!isSingleSpan) {
                targetRect.top = offsetTop;
                targetRect.bottom = offsetBottom;
            }
            if (isFirstRow && isLastRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.left = mBuilder.paddingStart;
                }
                if (!mBuilder.isHiddenLast) {
                    targetRect.right = mBuilder.paddingEnd;
                }
            } else if (isFirstRow) {
                if (!mBuilder.isHiddenFirst) {
                    targetRect.left = mBuilder.paddingStart;
                }
                targetRect.right = mBuilder.horizontalSpace;
            } else if (isLastRow) {
                if (!mBuilder.isHiddenLast) {
                    targetRect.right = mBuilder.paddingEnd;
                }
            } else {
                targetRect.right = mBuilder.horizontalSpace;
            }
        }

        outRect.set(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom);
    }

    /**
     * 绘制流式布局分割线
     *
     * @param c           画布
     * @param orientation 布局方向
     * @param parent      RecycleView
     * @param child       Child View
     */
    private void drawStaggeredGridBoundRect(Canvas c, int orientation, RecyclerView parent, View child) {
        // 保留
    }

    /**
     * 绘制线性布局分割线
     *
     * @param c           画布
     * @param orientation 布局方向
     * @param parent      RecycleView
     * @param child       Child View
     */
    private void drawLinearLayoutBoundRect(Canvas c, int orientation, RecyclerView parent, View child) {

        final int position = parent.getChildAdapterPosition(child);

        if (position == mBuilder.skipStart && mBuilder.isHiddenFirst) {
            return;
        }
        if (position + mBuilder.endSkip == parent.getAdapter().getItemCount() - 1 && mBuilder.isHiddenLast) {
            return;
        }

        final Rect targetRect = getLinearLayoutBoundRect(orientation, parent, child);

        c.drawLine(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom, mPaint);
    }

    /**
     * @param c           画布
     * @param orientation 布局方向
     * @param parent      RecycleView
     * @param child       Child View
     */
    private void drawGridLayoutBoundRect(Canvas c, int orientation, RecyclerView parent, View child) {

        final int position = parent.getChildAdapterPosition(child);
        final int itemCount = parent.getAdapter().getItemCount();

        final Rect vLeftRect = new Rect();
        final Rect vRightRect = new Rect();
        final Rect hTopRect = new Rect();
        final Rect hBottomRect = new Rect();

        final int dividerHalf = round(mBuilder.dividerSize * 1.0f / 2);
        final int vHalfSize = max(mBuilder.dividerSize, mBuilder.verticalSpace) / 2;
        final int hHalfHSize = max(mBuilder.dividerSize, mBuilder.horizontalSpace) / 2;

        // 不区分方向，相对于 View 本身环绕
        hTopRect.left = child.getLeft() - hHalfHSize - dividerHalf;
        hTopRect.top = child.getTop() - vHalfSize;
        hTopRect.right = child.getRight() + hHalfHSize;
        hTopRect.bottom = hTopRect.top;

        hBottomRect.left = hTopRect.left;
        hBottomRect.top = child.getBottom() + vHalfSize;
        hBottomRect.right = hTopRect.right;
        hBottomRect.bottom = hBottomRect.top;

        vLeftRect.left = child.getLeft() - hHalfHSize;
        vLeftRect.top = child.getTop() - vHalfSize - dividerHalf;
        vLeftRect.right = vLeftRect.left;
        vLeftRect.bottom = child.getBottom() + vHalfSize;

        vRightRect.left = child.getRight() + hHalfHSize;
        vRightRect.top = vLeftRect.top;
        vRightRect.right = vRightRect.left;
        vRightRect.bottom = vLeftRect.bottom;

        final boolean isHiddenFirstOrLast = mBuilder.isHiddenFirst || mBuilder.isHiddenLast;
        if (isHiddenFirstOrLast && mSpanInfo != null) {
            final int firstRow = mSpanInfo.getRowByPosition(mBuilder.skipStart);
            final int lastRow = mSpanInfo.getRowByPosition(itemCount - mBuilder.endSkip - 1);

            boolean isFirstRow = mSpanInfo.isInRow(position, firstRow);
            boolean isLastRow = mSpanInfo.isInRow(position, lastRow);

            if (orientation == HORIZONTAL) {
                c.drawLine(hTopRect.left, hTopRect.top, hTopRect.right, hTopRect.bottom, mPaint);
                c.drawLine(hBottomRect.left, hBottomRect.top, hBottomRect.right, hBottomRect.bottom, mPaint);
                if (!isFirstRow) {
                    c.drawLine(vLeftRect.left, vLeftRect.top, vLeftRect.right, vLeftRect.bottom, mPaint);
                }
                if (!isLastRow) {
                    c.drawLine(vRightRect.left, vRightRect.top, vRightRect.right, vRightRect.bottom, mPaint);
                }
            } else if (orientation == VERTICAL) {
                c.drawLine(vLeftRect.left, vLeftRect.top, vLeftRect.right, vLeftRect.bottom, mPaint);
                c.drawLine(vRightRect.left, vRightRect.top, vRightRect.right, vRightRect.bottom, mPaint);
                if (!isFirstRow) {
                    c.drawLine(hTopRect.left, hTopRect.top, hTopRect.right, hTopRect.bottom, mPaint);
                }
                if (!isLastRow) {
                    c.drawLine(hBottomRect.left, hBottomRect.top, hBottomRect.right, hBottomRect.bottom, mPaint);
                }
            }
        } else {
            c.drawLine(vLeftRect.left, vLeftRect.top, vLeftRect.right, vLeftRect.bottom, mPaint);
            c.drawLine(vRightRect.left, vRightRect.top, vRightRect.right, vRightRect.bottom, mPaint);

            c.drawLine(hTopRect.left, hTopRect.top, hTopRect.right, hTopRect.bottom, mPaint);
            c.drawLine(hBottomRect.left, hBottomRect.top, hBottomRect.right, hBottomRect.bottom, mPaint);
        }
    }

    /**
     * @param orientation 布局方向
     * @param parent      RecycleView
     * @param child       Child View
     * @return 获View的范围
     */
    private Rect getLinearLayoutBoundRect(int orientation, RecyclerView parent, View child) {

        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

        final Rect rect = new Rect();
        if (orientation == RecyclerView.VERTICAL) {
            int halfSize = max(mBuilder.dividerSize, mBuilder.verticalSpace) / 2;

            rect.left = round(child.getTranslationX());
            rect.top = child.getBottom() + halfSize + params.topMargin;
            rect.right = parent.getWidth() + round(child.getTranslationY());
            rect.bottom = rect.top;

            if (mBuilder.dividerPaddingEnable) {
                rect.left += mBuilder.paddingStart;
                rect.right -= mBuilder.paddingEnd;
            }
        } else if (orientation == RecyclerView.HORIZONTAL) {
            int halfSize = max(mBuilder.dividerSize, mBuilder.horizontalSpace) / 2;

            rect.left = child.getRight() + params.getMarginStart() + round(child.getTranslationX()) + halfSize;
            rect.top = round(child.getTranslationY());
            rect.right = rect.left;
            rect.bottom = parent.getHeight() + round(child.getTranslationY());

            if (mBuilder.dividerPaddingEnable) {
                rect.top += mBuilder.paddingTop;
                rect.bottom -= mBuilder.paddingBottom;
            }
        }

        return rect;
    }

    /**
     * @param parent RecycleView
     * @return 是否反转布局
     */
    private boolean isReverseLayout(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getReverseLayout();
        } else {
            return false;
        }
    }

    /**
     * 建造器
     */
    public static class DividerBuilder {
        private Context context;
        private boolean isHiddenFirst;// 是否隐藏第一个（topPadding 控制）
        private boolean isHiddenLast;// 是否隐藏最后一个（bottomPadding 控制）
        private int skipStart;// 跳过开始Item数量
        private int endSkip;// 跳过结尾Item数量
        @Dimension(unit = Dimension.DP)
        private int verticalSpace;// 垂直方向间距
        private boolean verticalOuter;// 外边距
        @Dimension(unit = Dimension.DP)
        private int horizontalSpace;// 水平方向间距
        private boolean horizontalOuter;// 外边距
        @Dimension(unit = Dimension.DP)
        private int paddingStart;// 外边距：左侧
        @Dimension(unit = Dimension.DP)
        private int paddingEnd;// 外边距：右侧
        @Dimension(unit = Dimension.DP)
        private int paddingTop;// 外边距：底部
        @Dimension(unit = Dimension.DP)
        private int paddingBottom;// 外边距：底部
        @ColorRes
        private int colorRes;// 分割线颜色
        @DrawableRes
        private int drawableRes;// 分割线图片(待完善)
        @Dimension(unit = Dimension.DP)
        private int dividerSize;// 分割线尺寸
        private boolean dividerPaddingEnable;// 分割线padding
        private int dividerGravity = Gravity.CENTER;// (待完善)
        private VisibilityProvider visibilityProvider = (position, parent) -> false;

        public DividerBuilder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * @param hiddenFirst 是否隐藏第一个
         * @return DividerBuilder
         */
        public DividerBuilder setHiddenFirst(boolean hiddenFirst) {
            isHiddenFirst = hiddenFirst;
            return this;
        }

        /**
         * @param hiddenLast 是否隐藏最后一个
         * @return DividerBuilder
         */
        public DividerBuilder setHiddenLast(boolean hiddenLast) {
            isHiddenLast = hiddenLast;
            return this;
        }

        /**
         * @param skipStart 跳过开始Item数量
         * @return DividerBuilder
         */
        public DividerBuilder setSkipStart(int skipStart) {
            this.skipStart = skipStart;
            return this;
        }

        /**
         * @param skipEnd 跳过结束Item数量
         * @return DividerBuilder
         */
        public DividerBuilder setSkipEnd(int skipEnd) {
            this.endSkip = skipEnd;
            return this;
        }

        /**
         * @param verticalSpace 垂直方向间距
         * @return DividerBuilder
         */
        public DividerBuilder setVerticalSpace(@Dimension(unit = DP) int verticalSpace) {
            this.verticalSpace = dp2px(context, verticalSpace);
            return this;
        }

        /**
         * @param horizontalSpace 水平放下间距
         * @return DividerBuilder
         */
        public DividerBuilder setHorizontalSpace(@Dimension(unit = DP) int horizontalSpace) {
            this.horizontalSpace = dp2px(context, horizontalSpace);
            return this;
        }

        /**
         * @param verticalSpaceOuter 是否显示纵向外部的边距
         * @return DividerBuilder
         */
        public DividerBuilder setVerticalOuter(boolean verticalSpaceOuter) {
            this.verticalOuter = verticalSpaceOuter;
            return this;
        }

        /**
         * @param horizontalSpaceOuter 是否显示横向外部的边距
         * @return DividerBuilder
         */
        public DividerBuilder setHorizontalOuter(boolean horizontalSpaceOuter) {
            this.horizontalOuter = horizontalSpaceOuter;
            return this;
        }

        /**
         * @param paddingStart 左侧外边距
         * @return DividerBuilder
         */
        public DividerBuilder setPaddingStart(@Dimension(unit = DP) int paddingStart) {
            this.paddingStart = dp2px(context, paddingStart);
            return this;
        }

        /**
         * @param paddingEnd 右侧外边距
         * @return DividerBuilder
         */
        public DividerBuilder setPaddingEnd(@Dimension(unit = DP) int paddingEnd) {
            this.paddingEnd = dp2px(context, paddingEnd);
            return this;
        }

        /**
         * @param paddingTop 顶部外边距
         * @return DividerBuilder
         */
        public DividerBuilder setPaddingTop(@Dimension(unit = DP) int paddingTop) {
            this.paddingTop = dp2px(context, paddingTop);
            return this;
        }

        /**
         * @param paddingBottom 底部外边距
         * @return DividerBuilder
         */
        public DividerBuilder setPaddingBottom(@Dimension(unit = DP) int paddingBottom) {
            this.paddingBottom = dp2px(context, paddingBottom);
            return this;
        }

        /**
         * @param dividerSize 分割线大小
         * @return DividerBuilder
         */
        public DividerBuilder setDividerSize(@Dimension(unit = DP) int dividerSize) {
            this.dividerSize = dp2px(context, dividerSize);
            return this;
        }

        /**
         * @param dividerPaddingEnable 分割线padding
         * @return DividerBuilder
         */
        public DividerBuilder setDividerPaddingEnable(boolean dividerPaddingEnable) {
            this.dividerPaddingEnable = dividerPaddingEnable;
            return this;
        }

        /**
         * @param colorRes 分割线颜色
         * @return DividerBuilder
         */
        public DividerBuilder setColorRes(@ColorRes int colorRes) {
            this.colorRes = colorRes;
            return this;
        }

        /**
         * @param drawableRes 分割线 drawable
         * @return DividerBuilder
         */
        public DividerBuilder setDrawableRes(@DrawableRes int drawableRes) {
            this.drawableRes = drawableRes;
            return this;
        }

        /**
         * @param visibilityProvider 过滤器
         * @return DividerBuilder
         */
        public DividerBuilder setVisibilityProvider(@NonNull VisibilityProvider visibilityProvider) {
            this.visibilityProvider = visibilityProvider;
            return this;
        }

        /**
         * @return DividerBuilder 构建实例
         */
        public UltimaSpaceItemDecoration build() {
            return new UltimaSpaceItemDecoration(context, this);
        }

        private int dp2px(Context context, @Dimension(unit = DP) float dpValue) {
            return Math.round(context.getResources().getDisplayMetrics().density * dpValue + 0.5f);
        }
    }

    /**
     * @return DividerBuilder Divider 过滤器，控制是否显示
     */
    public interface VisibilityProvider {
        /**
         * @param position Item Position
         * @param parent   RecycleView
         * @return 是否过滤
         */
        boolean filter(int position, RecyclerView parent);
    }

    class SpanInfo {
        private List<Integer> firstSpanPositions = new ArrayList<>();
        private List<Integer> lastSpanPositions = new ArrayList();
        private Map<Integer, Integer> positionRowMapper = new LinkedHashMap<>();
        private Map<Integer, List<Integer>> rowPositionMapper = new LinkedHashMap<>();
        private Map<Integer, Integer> rowSumSpanCount = new LinkedHashMap<>();
        private Map<Integer, Integer> rowOffsetIndex = new LinkedHashMap<>();

        SpanInfo(RecyclerView parent, GridLayoutManager layoutManager) {
            final RecyclerView.Adapter adapter = parent.getAdapter();
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
            final int spanCount = layoutManager.getSpanCount();

            int row = 0;
            int resetWrapTotalSpanSize = 0;
            for (int i = 0; i < adapter.getItemCount(); i++) {

                final int innerSpanSize = spanSizeLookup.getSpanSize(i);
                resetWrapTotalSpanSize += innerSpanSize;

                List<Integer> positionList;
                if (resetWrapTotalSpanSize == spanCount) {

                    positionList = rowPositionMapper.get(row);
                    if (positionList == null || positionList.isEmpty()) {
                        positionList = new ArrayList<>();
                        rowPositionMapper.put(row, positionList);
                        firstSpanPositions.add(i);
                    }
                    positionList.add(i);
                    positionRowMapper.put(i, row);

                    lastSpanPositions.add(i);

                    rowSumSpanCount.put(row, resetWrapTotalSpanSize);
                    rowOffsetIndex.put(i, (resetWrapTotalSpanSize - 1) % spanCount);

                    resetWrapTotalSpanSize = 0;
                    row++;
                } else if (resetWrapTotalSpanSize > spanCount) {

                    rowSumSpanCount.put(row, resetWrapTotalSpanSize - innerSpanSize);
                    positionList = rowPositionMapper.get(++row);
                    if (positionList == null || positionList.isEmpty()) {
                        positionList = new ArrayList<>();
                        rowPositionMapper.put(row, positionList);
                        firstSpanPositions.add(i);
                    }
                    positionList.add(i);
                    positionRowMapper.put(i, row);

                    rowOffsetIndex.put(i, (resetWrapTotalSpanSize - innerSpanSize - 1) % spanCount);

                    resetWrapTotalSpanSize = innerSpanSize;

                    rowSumSpanCount.put(row, resetWrapTotalSpanSize);
                } else {

                    positionList = rowPositionMapper.get(row);
                    if (positionList == null || positionList.isEmpty()) {
                        positionList = new ArrayList<>();
                        rowPositionMapper.put(row, positionList);
                        firstSpanPositions.add(i);
                    }
                    positionList.add(i);
                    positionRowMapper.put(i, row);

                    rowSumSpanCount.put(row, resetWrapTotalSpanSize);

                    rowOffsetIndex.put(i, (resetWrapTotalSpanSize - 1) % spanCount);
                }
            }

//            Log.d(TAG, "FirstSpan:" + firstSpanPositions);
//            Log.d(TAG, "LastSpan:" + lastSpanPositions);
//            Log.d(TAG, "RowPositionMapper:" + rowPositionMapper);
//            Log.d(TAG, "PositionRowMapper:" + positionRowMapper);
//            Log.d(TAG, "RowSumSpanCount:" + rowSumSpanCount);
//            Log.d(TAG, "RowOffsetIndex:" + rowOffsetIndex);
        }

        boolean isFirstSpan(int position) {
            return firstSpanPositions.contains(position);
        }

        boolean isLastSpan(int position) {
            return lastSpanPositions.contains(position);
        }

        int getRowByPosition(int position) {
            return positionRowMapper.get(position);
        }

        int getCurrentRowRelativeIndex(int position) {
            final int currRow = getRowByPosition(position);
            return rowPositionMapper.get(currRow).indexOf(position);
        }

        int getCurrentRowRelativeOffsetIndex(int position) {
            return rowOffsetIndex.get(position);
        }

        int getCurrentRowRelativeItemCount(int position) {
            final int currRow = getRowByPosition(position);
            return rowPositionMapper.get(currRow).size();
        }

        int getCurrentRowSumSpanCount(int position) {
            final int currRow = getRowByPosition(position);
            return rowSumSpanCount.get(currRow);
        }

        boolean isInRow(int position, int row) {
            return getRowByPosition(position) == row;
        }
    }

}