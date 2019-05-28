

void Gain_d_To_Gain(float *gd,float *g,float *memgi_1,float *memgdi_1,float alphaA,float betaA,float alphaR,float betaR,int len)
{
	int i;
	
	if (gd[0] <= *memgi_1)
	{
		g[0] = alphaA * gd[0] + betaA * (*memgi_1);
	} 
	else
	{
		g[0] = alphaR * gd[0] + betaR * (*memgi_1);
	}
	for (i=1;i<len;i++)
	{
		if (gd[i] <= g[i-1])
		{
			g[i] = alphaA * gd[i] + betaA * g[i-1];
		} 
		else
		{
			g[i] = alphaR * gd[i] + betaR * g[i-1];
		}
	}
	*memgi_1 = g[len-1];
	*memgdi_1 = gd[len-1];
}