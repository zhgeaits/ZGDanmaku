precision highp float;          //设置高精度
uniform mat4 uMVPMatrix;        //总变换矩阵
attribute vec3 aPosition;       //顶点位置
attribute vec2 aTexCoor;        //纹理坐标
varying vec2 vTextureCoord;     //用于传递给片元着色器的变量
uniform float offsetX;          //偏移的X坐标
uniform float offsetY;          //偏移的Y坐标
uniform float offsetZ;          //偏移的Z坐标
uniform float mViewHeight;      //屏幕高
uniform float mViewWidth;       //屏幕宽

//对应java层的Matrix.translateM()方法,但是我还是不知道为什么不能循环,一旦循环就崩溃
mat4 translateM(float x, float y, float z)
{
    mat4 translate = uMVPMatrix;
    translate[3][0] += translate[0][0] * x + translate[1][0] * y + translate[2][0] * z;
    translate[3][1] += translate[0][1] * x + translate[1][1] * y + translate[2][1] * z;
    translate[3][2] += translate[0][2] * x + translate[1][2] * y + translate[2][2] * z;
    translate[3][3] += translate[0][3] * x + translate[1][3] * y + translate[2][3] * z;
    return translate;
}

mat4 getFinalMatrix()
{
    float unitX;
    float unitY;
    float unitZ;

    //弹幕平移，同理坐标放大两倍
    unitX = offsetX / mViewWidth * 2.0;
    unitY = offsetY / mViewHeight * 2.0;
    unitZ = 0.0;

    mat4 final = translateM(unitX, unitY, unitZ);
    return final;
}

void main()     
{
    mat4 final;
    final = getFinalMatrix();
    gl_Position = final * vec4(aPosition,1); //根据总变换矩阵计算此次绘制此顶点位置
    vTextureCoord = aTexCoor;//将接收的纹理坐标传递给片元着色器
}                      