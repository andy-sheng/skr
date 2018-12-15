进入房间以后，能带着以下信息

// 准备游戏通知消息
message ReadyNoticeMsg
{
    repeated ReadyInfo readyInfo = 1; //准备信息
    sint32 hasReadyedUserCnt     = 2; //已经准备人数
    bool isGameStart             = 3; //游戏是否开始
    repeated RoundInfo roundInfo = 4; //轮次信息
    GameStartInfo gameStartInfo            = 5; // 游戏信息
}

message ReadyInfo
{
    uint32 userID      = 1; //用户id
    uint32 readySeq    = 2; //准备顺序
    sint64 readyTimeMs = 3; //准备时间戳
}

message RoundInfo
{
    uint32 userID       = 1; //玩家id
    uint32 playbookID   = 2; //曲库id
    uint32 roundSeq     = 3; //轮次顺序
    uint32 singBeginMs = 4; //演唱开始相对时间（相对于startTimeMs时间）
    uint32 singEndMs   = 5; //演唱结束相对时间（相对于startTimeMs时间）
}

message GameStartInfo
{
    sint64 startTimeMs   = 1; //开始时间戳
    sint64 startPassedMs = 2; //已经开始时间
}


能准确知道每个人开唱的一些信息，其中歌曲只有id部分。在请求匹配时{{game-domain}}/v1/game/query-match写入服务器。

默认进入房间时，就是首轮开始，因为客户端时间和服务器时间有误差。那我们默认游戏开始这个请求 GameStartInfo 的延时是最小的（既然认为这个数据包到客户端没有延迟）。记录下当时的本地时间
localStartTimeMs。可以估算出各个轮次结束时的本地时间。

各个端进入房间时，就自动算出当前的应该的轮次信息，以及当前实际的轮次信息。当前应该的轮次信息和实际的轮次信息不匹配时，就要进行轮次信息更新，并进行相应的动作。


上报结束一轮游戏
@PUT("http://dev.game.inframe.mobi/v1/game/round/over")

当前轮次时上报心跳
@PUT("http://dev.game.inframe.mobi/v1/game/hb")


同步游戏详情状态
@GET("http://dev.game.inframe.mobi/v1/game/status")

long syncStatusTimes = result.getData().getLong("syncStatusTimeMs");  //状态同步时的毫秒时间戳
long gameOverTimeMs = result.getData().getLong("gameOverTimeMs");  //游戏结束时间

List<JsonOnLineInfo> onlineInfos = JSON.parseArray(result.getData().getString("onlineInfo"), JsonOnLineInfo.class); //在线状态
	JsonOnLineInfo 
		id online

JsonRoundInfo currentInfo = JSON.parseObject(result.getData().getString("currentRound"), JsonRoundInfo.class); //当前轮次信息
	当前 JsonRoundInfo

JsonRoundInfo nextInfo = JSON.parseObject(result.getData().getString("nextRound"), JsonRoundInfo.class); //下个轮次信息
	下个 JsonRoundInfo


退出游戏
@PUT("http://dev.game.inframe.mobi/v1/game/exit")
    
