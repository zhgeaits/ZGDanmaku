precision mediump float;
varying vec2 vTextureCoord; //接收的纹理坐标
uniform sampler2D sTexture;//纹理内容数据
void main()                         
{           
   //给此片元从纹理中采样出颜色值            
   gl_FragColor = texture2D(sTexture, vTextureCoord); 
}              