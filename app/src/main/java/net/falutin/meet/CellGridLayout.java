package net.falutin.meet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Lays out grid cells dynamically and supports grid path selection
 * Created by sokolov on 2/28/2015.
 */
public class CellGridLayout extends RelativeLayout {

    private int cellSize = 0;
    private int dim;
    private CellGrid grid;
    private CanvasView canvasView;
    private byte[] cellPath;
    private byte pathLength;
    private int cellTextColor, gestureColor, alreadyColor;
    private boolean enabled;

    public enum SelectionKind {
        FOUND, ALREADY, NONE
    }

    public CellGridLayout (Context context, AttributeSet attrs) {
        super (context, attrs);
        gestureColor = getResources().getColor(R.color.gestureColor);
        alreadyColor = getResources().getColor(R.color.alreadyColor);
        cellTextColor = getResources().getColor(android.R.color.black);
    }

    @Override
    protected  void onMeasure (int w, int h) {
        int max = Math.max(w, h);
        super.onMeasure(max, max);
    }

    private void createCellPath (int size) {
        cellPath = new byte[size + 1];
        for (int i = 0; i < cellPath.length; i++) {
            cellPath[i] = -1;
        }
        pathLength = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int size = Math.min(r - l, b - t);
        // The first N^2 children are the grid cells
        int childCount = getChildCount();
        Log.d(TAG, "onLayout() called with: changed = [" + changed + "], l = [" + l + "], t = [" + t + "], r = [" + r + "], b = [" + b + "]");
        Log.e(TAG, "onLayout: childCount: " + childCount);
        dim = (int) Math.sqrt(childCount);
        createCellPath(dim * dim);
        cellSize = size / dim;
        for (int i = dim * dim - 1; i >= 0; i--) {
            final TextView cell = getCell(i);
            int row = i / dim;
            int col = i % dim;
            // cell.setPadding(0, cellSize/6, 0, 0);
            //TODO: For spacing between grids
            //cell.layout(col * cellSize + cellSize/6, row * cellSize + cellSize/6,
            //                    (col+1) * cellSize - cellSize/6, (row+1) * cellSize - cellSize/6);
            cell.layout(col * cellSize, row * cellSize,
                    (col+1) * cellSize, (row+1) * cellSize);
        }
        if (grid != null) {
            setGrid(grid); // copy the letters into the text cells
        }
        if (canvasView != null) {
            // canvasView will be null in the development studio layout tool
            canvasView.layout(l, t, r, b);
            canvasView.setDimensions(dim, cellSize, cellPath);
        }
    }

    public void setCanvasView(CanvasView canvasView) {
        this.canvasView = canvasView;
    }

    private TextView getCell (int idx) {
        return (TextView) getChildAt(idx);
    }

    public int getSelectedCellIndex(MotionEvent event) {
        int row = (int) event.getY() / cellSize;
        int col = (int) event.getX() / cellSize;
        double dr = (event.getY() - (row + 0.5) * cellSize);
        double dc = (event.getX() - (col + 0.5) * cellSize);
        //TODO: After spacing between two grids,
        // if (dr*dr + dc*dc > cellSize*cellSize/6)
        if (dr*dr + dc*dc > cellSize*cellSize/4) {
            // the "hot" area is the circle inscribed within each cell with radius 6^(-0.5);
            // somewhere between 1/2 and 1/3.
            return -1;
        }
        return row * dim + col;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!enabled) {
            return false;
        }
        final int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_UP) {
            // up events are handled by the enclosing activity; bubble up
            return false;
        }
        int cellIndex = getSelectedCellIndex(event);
        if (cellIndex >= 0 && cellIndex < dim*dim) {
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    initPath(cellIndex);

                case MotionEvent.ACTION_MOVE:
                    addPath(cellIndex);
            }
        }
        return true;
    }

    private void initPath(int cellIndex) {
        cellPath[0] = (byte) cellIndex;
        cellPath[1] = -1;
        pathLength = 1;
        //highlightSelection(SelectionKind.NONE);
        selectCell(cellIndex);
    }

    private void addPath(int cellIndex) {
        assert(cellIndex < dim*dim);
        for (int i = pathLength-1; i >= 0; i--) {
            if (cellPath[i] == cellIndex) {
                // this cell is already selected and we don't allow repetitions
                return;
            }
        }
        if (pathLength > 0) {
            if (! invalidateIfValidSelection(cellIndex)) {
                return;
            }
        }
        cellPath[pathLength++] = (byte) cellIndex;
        cellPath[pathLength] = -1;
        selectCell(cellIndex);
    }

    private boolean invalidateIfValidSelection(int cellIndex) {
        int x0 = cellPath[pathLength - 1] % dim;
        int y0 = cellPath[pathLength - 1] / dim;
        int x1 = cellIndex % dim;
        int y1 = cellIndex / dim;
        int dist = Math.max(Math.abs(y0 - y1), Math.abs(x0 - x1));
        if (dist != 1) {
            // can only select cells that are 1 away, using the max norm.
            return false;
        }
        if (x0 > x1) {
            int tmp = x0; x0 = x1; x1 = tmp;
        }
        if (y0 > y1) {
            int tmp = y0; y0 = y1; y1 = tmp;
        }
        canvasView.invalidate(x0*cellSize, y0*cellSize, (x1+1)*cellSize, (y1+y1) * cellSize);
        return true;
    }

    public String getSelectedWord () {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < pathLength; i++) {
            buf.append(getCell(cellPath[i]).getText());
            Log.d(TAG, "getSelectedWord() called: letter is: " + getCell(cellPath[i]).getText() + " and index is: " + i);
        }
        return buf.toString();
    }

    public void clearPath() {
        Log.d(TAG, "clearPath() called");
        pathLength = 0;
        cellPath[0] = -1;
        canvasView.invalidate();
    }

    private static final String TAG = "CellGridLayout";

    public void highlightSelection (SelectionKind sel) {
        Log.d(TAG, "highlightSelection() called with: sel = [" + sel + "]");
        switch (sel) {
            case NONE: default:
                drawSelection(R.drawable.tile_bg, cellTextColor);
                return;
            case FOUND:
                drawSelection(R.drawable.found_tile_bg, gestureColor);
                break;
            case ALREADY:
                drawSelection(R.drawable.tile_bg, alreadyColor);
                break;
        }
        clearSelectionDelayed(200);
    }

    private void drawSelection(int shape, int color) {
        for (int i = 0; i < pathLength; i++) {
            final TextView cell = getCell(cellPath[i]);
            cell.setBackgroundResource(shape);
            cell.setTextColor(color);
        }
    }

    private void drawAllCells(int shape, int color) {
        Log.d(TAG, "drawAllCells() called with: shape = [" + shape + "], color = [" + color + "]");
        for (int i = 0; i < dim*dim; i++) {
            final TextView cell = getCell(i);
            cell.setBackgroundResource(shape);
            cell.setTextColor(color);
        }
    }

    public void clearSelection() {
        drawAllCells(R.drawable.tile_bg, cellTextColor);
    }

    public void dimGrid() {
        drawAllCells(R.drawable.found_tile_bg, alreadyColor);
    }

    private void clearSelectionDelayed (int msec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run() called");
                clearSelection();
            }
        }, msec);
    }

    public void selectCell (int cellIndex) {
        Log.d(TAG, "selectCell() called with: cellIndex = [" + cellIndex + "]");
        View child = getChildAt(cellIndex);
        if (child != null) {
            child.setBackgroundResource(R.drawable.selected_tile_bg);
        } else {
            Log.w(WordSearch.TAG, "cell index out of bounds in selectCell: " + cellIndex);
        }
    }

    public CellGrid getGrid() {
        return grid;
    }

    @SuppressLint("SetTextI18n")
    public void setGrid(CellGrid grid) {
        this.grid = grid;
        enabled = true;
        if (cellSize == 0) {
            // onLayout not yet called
            return;
        }
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                TextView cell = getCell(row * grid.width() + col);
                final char c = grid.get(row, col);
//                if (c == 'Q') {
//                    cell.setText("QU");
//                    cell.setTextScaleX(0.45f);
//                } else {
                    cell.setText(new String(new char[]{c}));
//                }
            }
        }
    }

    public boolean isEnabled () {
        return enabled;
    }

    public void setEnabled (boolean enabled) {
        this.enabled = enabled;
    }

}