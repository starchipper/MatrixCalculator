package com.myblueshare.matrixcalculator.matrixcalculator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

public class InverseDisplay extends AppCompatActivity {

    private int dims;
    private double[][] matrix;
    private double[][] inverseMatrix;
    private double[][] identityMatrix;
    private String calcType;
    private boolean isInvertible = true;
    private GridLayout gd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inverse_display);

        Bundle extras = getIntent().getExtras();
        dims = extras.getInt("dims");
        calcType = extras.getString("calc_type");
        matrix = (double[][]) extras.getSerializable("matrix");

        //Use GridLayout Instead
        gd = (GridLayout) findViewById(R.id.gridI);
        gd.setRowCount(dims);
        gd.setColumnCount(dims);
        gd.setPadding(10,10,10,10);
        TextView txt;
        String strValue;

        inverseMatrix = new double[dims][dims];
        calculate(matrix);

        if(isInvertible)
        {
            for(int r = 0; r < dims; r++)
            {
                for(int c = 0; c < dims; c++)
                {
                    txt = new TextView(this);
                    strValue = Double.toString(inverseMatrix[r][c]);
                    txt.setText(strValue);
                    txt.setWidth(100);
                    txt.setHeight(100);
                    gd.addView(txt);
                }
            }
        }
        else
        {
            //Error message turns visible
            TextView errortxt = (TextView) findViewById(R.id.textView31);
            errortxt.setVisibility(View.VISIBLE);
        }
    }

    private void calculate(double[][] matrix)
    {
        //create an identity matrix out of the dimension size
        final double ONE = 1;
        identityMatrix = new double[dims][dims];
        for(int p=0; p < identityMatrix.length; p++)
        {
            identityMatrix[p][p] = ONE;
        }

        //create a matrix that combines the matrix and identityMatrix together
        int jointColumns = dims + dims;
        double[][] jointMatrix = new double[dims][jointColumns];
        //how to combine the two matrices together?
        //Use System.arraycopy. It works!
        for(int r=0; r < dims; r++)
        {
            System.arraycopy(matrix[r], 0, jointMatrix[r], 0, matrix[r].length);
            System.arraycopy(identityMatrix[r], 0, jointMatrix[r], matrix[r].length,
                    identityMatrix[r].length);
        }

        //Row reduce the jointMatrix by using the ReducedRowEchelon algorithm
        jointMatrix = rref(jointMatrix);

        //split up the matrix and check if there are pivots in every diagonal on the first part of it
        double[][] reducedMatrix = new double[dims][dims]; //the first part of the jointMatrix
        //inverseMatrix is the second part of the jointMatrix
        for(int r2=0; r2 < dims; r2++)
        {
            for(int c2=0; c2 < dims; c2++)
            {
                reducedMatrix[r2][c2] = jointMatrix[r2][c2];
            }
        }
        //Now check if there is a pivot in the diagonal of reducedMatrix
        //If there is no pivot in every diagonal then the matrix is NOT invertible
        for(int d = 0; d < dims; d++)
        {
            if(reducedMatrix[d][d] != 1)
            {
                isInvertible = false;
            }
        }

        if(isInvertible)
        {
            //Fill in the _inverseMatrix
            for(int i=0; i < dims; i++)
            {
                for(int j=0; j < dims; j++)
                {
                    inverseMatrix[i][j] = jointMatrix[i][j+dims];
                }
            }
        }
    }

    private double[][] swapRows(double[][] matrix) //Swap the rows
    {
        double[][] swappedMatrix;
        int c = 0; //column iterator
        //increases by 1 everytime a pivot is found in order to swap pivotRows to the top correctly
        int pivots = 0;

        //checks if there's a zero column and checks if there are only zeros before a non-zero
        int zeroCounter = 0;
        double element;
        double[] rowHolder = new double[matrix.length]; //holds on to one row during the swap

        while (c < matrix[0].length)
        {
            //iterate through the first row

            for(int r=0; r<matrix.length; r++)
            {
                element = matrix[r][c];
                if(element != 0)
                {
                    //check if there are zeros above it
                    if(r == zeroCounter)
                    {
                        //check if there are no more NON pivot rows for that row
                        if(pivots < matrix.length)
                        {
                            //then the swap occurs
                            rowHolder = matrix[r];
                            matrix[r] = matrix[pivots];
                            matrix[pivots] = rowHolder;

                            c++; //go to next column
                            //check if we are at the last column
                            if (c < matrix[0].length)
                            {
                                pivots++;
                                r=pivots-1; //draw a diagram to make this work
                                zeroCounter=pivots;
                            }
                            else
                            {
                                r = matrix.length;
                            }
                        }
                    }
                }
                else //theres a zero in the column
                {
                    zeroCounter++;
                }

                //check if the column has ALL zeros (aka, a Zero Column)
                if(zeroCounter >= matrix.length)
                {
                    c++; //go to next column
                    //check if we reach the last column
                    if (c < matrix[0].length)
                    {
                        r=pivots-1; //draw a diagram to make this work
                        zeroCounter=pivots;
                    }
                    else
                    {
                        r = matrix.length;
                    }
                }
            }

        }
        swappedMatrix = matrix;
        return swappedMatrix;
    }

    private double[][] rref(double[][] matrix)
    {
        double[][] rrefMatrix = matrix;
        rrefMatrix = swapRows(rrefMatrix);
        int rows = rrefMatrix.length;
        int columns = rrefMatrix[0].length;

        if(rows <= columns)
        {
            for (int p = 0; p < rrefMatrix.length; ++p)
            {
                //Make this pivot into a 1
                double pivot = rrefMatrix[p][p]; //Very first element is pivot if it's not zero
                if (pivot != 0)
                {
                    //SCALE THE PIVOT AND EVERYTHING ELSE IN THE ROW MUST BE MULTIPLIED WITH IT
                    double scalar = 1 / pivot;
                    for (int i = 0; i < rrefMatrix[p].length; ++i) //interate through the ROW
                    {
                        rrefMatrix[p][i] = rrefMatrix[p][i] * scalar;
                    }
                }

                //Make other rows zero
                for (int r = 0; r < rrefMatrix.length; ++r)
                {
                    if (r != p)
                    {
                        double f = rrefMatrix[r][p];
                        for (int i = 0; i < rrefMatrix[r].length; ++i)
                        {
                            rrefMatrix[r][i] = rrefMatrix[r][i] - f * rrefMatrix[p][i];
                            //Turn the weird negative zeros to positive zeros
                            //But how?
                        }
                    }
                }
            }
        }
        else if(rows > columns)
        {
            for (int p = 0; p < rrefMatrix[0].length; ++p)
            {
                //Make this pivot into a 1
                double pivot = rrefMatrix[p][p]; //Very first element is pivot if it's not zero
                if (pivot != 0)
                {
                    //SCALE THE PIVOT AND EVERYTHING ELSE IN THE ROW MUST BE MULTIPLIED WITH IT
                    double scalar = 1 / pivot;
                    for (int i = 0; i < rrefMatrix[p].length; ++i) //interate through the ROW
                    {
                        rrefMatrix[p][i] = rrefMatrix[p][i] * scalar;
                    }
                }

                //Make other rows zero
                for (int r = 0; r < rrefMatrix.length; ++r)
                {
                    if (r != p)
                    {
                        double f = rrefMatrix[r][p];
                        for (int i = 0; i < rrefMatrix[r].length; ++i)
                        {
                            rrefMatrix[r][i] = rrefMatrix[r][i] - f * rrefMatrix[p][i];
                            //Turn the weird negative zeros to positive zeros
                            //But how?
                        }
                    }
                }
            }
        }
        return rrefMatrix;
    }

    public void calcAgain(View view)
    {
        Intent intent = new Intent(this, SquareMatrixDim.class);
        intent.putExtra("calc_Type", calcType);
        startActivity(intent);
    }

    public void backToMenu(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
