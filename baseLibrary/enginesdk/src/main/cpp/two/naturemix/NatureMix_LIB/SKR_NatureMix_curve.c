#include "NatureMix_control.h"
#include <stdlib.h>
#include <assert.h>

void Options_for_TRAE_NatureMix(NatureMix_ID *mNatureMix)
{
	
	float overdb;

	mNatureMix->DyForNatureMix.DynamicAttackms = 1.0;
	mNatureMix->DyForNatureMix.DynamicReleasems = 130.0;
	mNatureMix->DyForNatureMix.SideChain = NOUSE;
	mNatureMix->DyForNatureMix.Bypass = 0;
	mNatureMix->LevelForNatureMix.LevelAttackms = 0.5;//暂时没用
	mNatureMix->LevelForNatureMix.LevelRealeasems = 250.0;



	mNatureMix->DyForNatureMix.neverclipping = 0;
	mNatureMix->DyForNatureMix.CurveOption.PLen = 2;

	if (mNatureMix->comlim == 1)
	{
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.5;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.5;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -2.7;//-2.4;必须小于overdb
	} 
	else if(mNatureMix->comlim == 0)
	{
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.5;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.5;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -1.20;//-2.4;必须小于overdb
	}
	else if(mNatureMix->comlim == 2)
	{
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.5;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.5;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -3.0;//-2.4;必须小于overdb
	}
	else if(mNatureMix->comlim == 3)
	{
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.5;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.5;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -3.0;//-2.4;必须小于overdb
		mNatureMix->DyForNatureMix.neverclipping = 1;
	}
	else
	{
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.5;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.5;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -1.20;//-2.4;必须小于overdb
		assert(0);
	}
	
	if (mNatureMix->mixnum == 1)//1路情况可以设bypass
	{
		overdb = 3.0;
	} 
	else
	{
		overdb = (mNatureMix->mixnum-1)*6.0*0.4;//
	}
	if (overdb < (mNatureMix->DyForNatureMix.CurveOption.b_db*mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db)/(mNatureMix->DyForNatureMix.CurveOption.b_db - mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db) + 0.01);
	{
		overdb = (mNatureMix->DyForNatureMix.CurveOption.b_db*mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db)/(mNatureMix->DyForNatureMix.CurveOption.b_db - mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db) + 0.01;
	}

	mNatureMix->DyForNatureMix.CurveOption.P_db[1].x_db = mNatureMix->DyForNatureMix.CurveOption.b_db/(1+mNatureMix->DyForNatureMix.CurveOption.b_db/overdb);               //-2.3;
	mNatureMix->DyForNatureMix.CurveOption.P_db[1].y_db = -mNatureMix->DyForNatureMix.CurveOption.b_db/overdb*mNatureMix->DyForNatureMix.CurveOption.P_db[1].x_db + mNatureMix->DyForNatureMix.CurveOption.b_db;//-2.3387;
}

void Options_for_TRAE_NatureMix_ForVMic(NatureMix_ID *mNatureMix)
{

	float overdb;

	mNatureMix->DyForNatureMix.DynamicAttackms = 1.0;
	mNatureMix->DyForNatureMix.DynamicReleasems = 130.0;
	mNatureMix->DyForNatureMix.SideChain = NOUSE;
	mNatureMix->DyForNatureMix.Bypass = 0;
	mNatureMix->LevelForNatureMix.LevelAttackms = 0.5;//暂时没用
	mNatureMix->LevelForNatureMix.LevelRealeasems = 250.0;

	mNatureMix->DyForNatureMix.neverclipping = 1;
	if (mNatureMix->comlim == 1)
	{
		mNatureMix->DyForNatureMix.CurveOption.PLen = 2;
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.8;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.8;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -3.3;//-2.4;必须小于overdb
	} 
	else if(mNatureMix->comlim == 5)
	{
		mNatureMix->DyForNatureMix.CurveOption.PLen = 2;
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -6.8;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -6.8;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -3.9;//-2.4;必须小于overdb
	}
	else if(mNatureMix->comlim == 0)
	{
		mNatureMix->DyForNatureMix.CurveOption.PLen = 2;
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.5;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.5;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -1.6;//-2.4;必须小于overdb
	}
	else
	{
		mNatureMix->DyForNatureMix.CurveOption.PLen = 2;
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db = -4.8;//v1:-28.0;//v2beta:-26.5
		mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db = -4.8;//v1:-19.0;//v2beta:-15.5

		mNatureMix->DyForNatureMix.CurveOption.k = 1.0;
		mNatureMix->DyForNatureMix.CurveOption.b_db = -2.0;//-2.4;必须小于overdb
		assert(0);
	}


	if (mNatureMix->mixnum == 1)//1路情况可以设bypass
	{
		overdb = 7.0;
	} 
	else
	{
		overdb = (mNatureMix->mixnum-1)*6.0*0.5;//
	}
	
	if (overdb < (mNatureMix->DyForNatureMix.CurveOption.b_db*mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db)/(mNatureMix->DyForNatureMix.CurveOption.b_db - mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db) + 0.01);
	{
		overdb = (mNatureMix->DyForNatureMix.CurveOption.b_db*mNatureMix->DyForNatureMix.CurveOption.P_db[0].x_db)/(mNatureMix->DyForNatureMix.CurveOption.b_db - mNatureMix->DyForNatureMix.CurveOption.P_db[0].y_db) + 0.01;
	}

	mNatureMix->DyForNatureMix.CurveOption.P_db[1].x_db = mNatureMix->DyForNatureMix.CurveOption.b_db/(1+mNatureMix->DyForNatureMix.CurveOption.b_db/overdb);               //-2.3;
	mNatureMix->DyForNatureMix.CurveOption.P_db[1].y_db = -mNatureMix->DyForNatureMix.CurveOption.b_db/overdb*mNatureMix->DyForNatureMix.CurveOption.P_db[1].x_db + mNatureMix->DyForNatureMix.CurveOption.b_db;//-2.3387;
}