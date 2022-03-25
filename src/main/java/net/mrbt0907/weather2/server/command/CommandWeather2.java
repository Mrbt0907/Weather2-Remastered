package net.mrbt0907.weather2.server.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.network.packets.PacketVolcanoObject;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherSystemServer;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.StormObject.Type;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;
import CoroUtil.util.CoroUtilCompatibility;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;

public class CommandWeather2 extends CommandBase
{
	private String[] args = null;
	
	@Override
	public String getName() {
		return "storm";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "command.storm.usage";
	}
	
	public String getUsage(int selection)
	{
		switch(selection)
		{
			case 0:
				return "config.usage";
			case 1:
				return "config.reload.success";
			case 2:
				return "create.usage";
			case 3:
				return "create.success";
			case 4:
				return "create.fail";
			case 5:
				return "kill.usage";
			case 6:
				return "kill.all.success";
			case 7:
				return "kill.all.fail";
			case 8:
				return "test.usage";
			case 9:
				return "test.temperature.success";
			case 10:
				return "test.humidity.success";
			case 11:
				return "test.weather.success";
			case 12:
				return "test.volcano.success";
			default:
				return "usage";
		}
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	public void execute2(MinecraftServer server, ICommandSender sender, String[] args)
	{
		World world = sender.getEntityWorld();
		Vec3d pos = sender.getPositionVector();
		this.args = args;
		
		if (exists(0))
		{
			switch(getLower(0))
			{
				case "config":
				{
					if (exists(1))
					{
						switch(getLower(1))
						{
							case "reload":
							{
								WeatherAPI.refreshGrabRules();
								say(sender, 1);
								break;
							}
							default:
							{
								say(sender, 0);
							}
						}
					}
					else
						say(sender, 0);
					break;
				}
				case "create":
				{
					if (exists(1))
					{
						boolean isCyclone = false;
						float intensity = 0.0F;
						float humidity = 0.0F;
						
						switch(getLower(1))
						{
							case "rain":
								humidity = 100.0F;
								break;
							case "thunder": case "lightning": case "thunderstorm": case "lightningstorm":
								intensity = 0.0F;
								humidity = 100.0F;
								break;
							case "cell": case "supercell": case "severe": case "severethunder": case "severethunderstorm": case "severelightning": case "severelightningstorm":
								intensity = 1.0F;
								humidity = 100.0F;
								break;
							case "hail": case "hailstorm":
								intensity = 2.0F;
								humidity = 100.0F;
								break;
							case "sandstorm":
								intensity = -1.0F;
								break;
							default:
							{
								intensity = 3.0F;
								humidity = 100.0F;
								isCyclone = getLower(1).contains("c");
								int stage = getLower(1).matches(".*\\d.*") ? Integer.parseInt(getLower(1).replaceAll("[^\\d*]", "")) : -1;
								
								if (stage > -1)
								{
									intensity += 1.0F * stage;
									humidity *= stage;
								}
								else
									intensity = -1.0F;
							}
						}
						
						if (intensity >= 0.0F)
						{
							boolean isViolent = false;
							boolean isNatural = false;
							boolean isFirenado = false;
							boolean neverDissipate = false;
							boolean alwaysProgress = false;
							float sizeMultiplier = 1.0F;
							float angle = -1.0F;
							float speed = -1.0F;
							
							if (exists(3))
							{
								BlockPos temp;
								try
								{
									temp = parseBlockPos2(sender, args, 2, true);
								}
								catch (NumberInvalidException e)
								{
									e.printStackTrace();
									say(sender, 4);
									return;
								}
								pos = new Vec3d(temp.getX(), temp.getY(), temp.getZ());
							}
								
							for (int i = 4;exists(i);i++)
							{
								switch(getLower(i))
								{
									case "alwaysprogress":
										alwaysProgress = true;
										break;
									case "isviolent": case "violent":
										isViolent = true;
										break;
									case "isnatural": case "natural":
										isNatural = true;
										break;
									case "isfirenado": case "firenado":
										isFirenado = true;
										break;
									case "neverdissipate": case "neverdie":
										neverDissipate = true;
										break;
								}
							}
							//TODO: make this handle non StormObject types better, currently makes instance and doesnt use that type if its a sandstorm
							int dimension = world.provider.getDimension();
							WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(dimension);
							StormObject so = new StormObject(wm);
							so.layer = 0; 
							so.player = sender instanceof EntityPlayer ? CoroUtilEntity.getName((EntityPlayer)sender) : CoroUtilEntity.getName(null);
							so.isNatural = isNatural;
							so.stormTemperature = 0.1F;
							so.pos = new Vec3(pos.x, StormObject.layers.get(so.layer), pos.z);
							so.stormRain = humidity;
							so.stormIntensity = intensity;
							so.stormSizeRate = sizeMultiplier;
							so.overrideNewAngle = angle;
							so.overrideNewMotion = speed;
							so.stormStage = (int) (intensity + 1.0F);
							so.stormStageMax = so.stormStage;
							so.alwaysProgresses = alwaysProgress;
							so.neverDissipate = neverDissipate;
							so.isFirenado = isFirenado;
							so.isViolent = isViolent;
							
							if (isCyclone)
								so.stormType = Type.WATER.getInt();
							so.init();
								
							if (so.isNatural)
								so.initRealStorm(null, null);
							else
							{
								so.updateType();
								so.stormSizeRate = Maths.random(0.75F, 1.35F);
								if (so.isViolent)
								{
									so.stormSizeRate += Maths.random(0.25F, 1.65F);
									if (so.stormStageMax < 9)
										so.stormStageMax += 1;
								}
							}
							wm.addStormObject(so);
							PacketWeatherObject.create(wm.getDimension(), so);
							String stage = "";
							switch(so.type.getStage())
							{
							case 0:
								stage = "Cloud";
								break;
							case 1:
								stage = "Rainstorm";
								break;
							case 2:
								stage = "Thunderstorm";
								break;
							case 3:
								if (so.stormStage == 2)
									stage = "Supercell";
								else
									stage = "Hailing Supercell";
								break;
							case 4:
								if(so.stormType == 1)
									stage = "Tropical Storm";
								else
									stage = "Tornado EF" + (so.stormStage - 4);
								break;
							case 5:
								stage = "Hurricane Category " + (so.stormStage - 4);
								break;
							}
							String flags = "";
							if (alwaysProgress)
							{
								flags += " Always Progresses";
							}
							if (isViolent)
							{
								if (flags.length() == 0)
									flags += " Violent Storm";
								else
									flags += ", Violent Storm";
							}
							if (isNatural)
							{
								if (flags.length() == 0)
									flags += " Starts Naturally";
								else
									flags += ", Starts Naturally";
							}
							if (isFirenado)
							{
								if (flags.length() == 0)
									flags += " Is A Firenado";
								else
									flags += ", Is A Firenado";
							}
							if (neverDissipate)
							{
								if (flags.length() == 0)
									flags += " Never Dissipates";
								else
									flags += ", Never Dissipates";
							}
							say(sender, 3, stage, Math.round(pos.x), Math.round(pos.z), flags);
							return;
						}
						else
						{
							say(sender, 4);
						}
					}
					else
						say(sender, 2);
					break;
				}
				case "kill":
				{
					if (exists(1))
					{
						WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
						
						List<WeatherObject> listStorms = wm.getWeatherObjects();
						int size = listStorms.size();
						if (size > 0)
						{
							for (int i = 0; i < size; i++)
							{
								WeatherObject wo = listStorms.get(i);
								if (wo instanceof WeatherObject)
								{
									WeatherObject so = wo;
									Weather2.debug("force killing storm ID: " + so.getUUID().toString());
									so.setDead();
								}
							}
							say(sender, 6, size);
						}
						else
							say(sender, 7);
					}
					else
						say(sender, 5);
					break;
				}
				case "test":
				{
					if (exists(1))
					{
						switch(getLower(1))
						{
							case "temperature":
							{
								BlockPos bpos = new BlockPos(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
								double temp = CoroUtilCompatibility.getAdjustedTemperature(world, world.getBiome(bpos), bpos);
								say(sender, 9, String.format("%01.01f", temp * 80.0D), String.format("%01.01f", ((temp * 80.0D) - 32.0D) * 5/9), String.format("%01.04f", temp));
								break;
							}
							case "humidity":
							{
								BlockPos bpos = new BlockPos(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
								double humidity = WeatherUtil.getHumidity(world, bpos);
								say(sender, 10, String.format("%01.01f", (humidity / 0.5D) * 100.0D), String.format("%01.04f", humidity));
								break;
							}
							case "weather":
							{
								WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(0);
								BlockPos bpos = new BlockPos(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
								float temp = CoroUtilCompatibility.getAdjustedTemperature(world, world.getBiome(bpos), bpos);
								float humidity = WeatherUtil.getHumidity(world, bpos);
								say(sender, 11, "\n", String.format("%01.01f", WeatherUtil.toFahrenheit(temp)), String.format("%01.01f", WeatherUtil.toCelsius(temp)), String.format("%01.01f", humidity * 100.0D), String.format("%01.01f", wm.windMan.windSpeedGlobal), String.format("%01.01f", humidity * temp * 100.0D));
								break;
							}
							case "volcano":
							{
								WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(0);
								VolcanoObject vo = new VolcanoObject(wm);
								vo.pos = new Vec3(pos);
								vo.init();
								wm.addVolcanoObject(vo);				
								PacketVolcanoObject.create(wm.getDimension(), vo);
								
								say(sender, 12);
								break;
							}
							default:
								say(sender, 8);
						}
					}
					else
						say(sender, 8);
					break;
				}
			}
		}
		else
			say(sender, -1);
	}
	
	private void say(ICommandSender sender, int translation_id, Object... args)
	{
		notifyCommandListener(sender, this, "command." + getName() + "." + getUsage(translation_id), args);
	}
	
	private boolean exists(int index)
	{
		return args != null && args.length > index;
	}
	
	public static BlockPos parseBlockPos2(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        BlockPos blockpos = sender.getPosition();
        return new BlockPos(parseDouble((double)blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), 0.0D, parseDouble((double)blockpos.getZ(), args[startIndex + 1], -30000000, 30000000, centerBlock));
    }
	
	private String getLower(int index)
	{
		return exists(index) ? args[index].toLowerCase() : null;
	}
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
		switch(args.length - 1)
		{
			case 0:
				return getListOfStringsMatchingLastWord(args, new String[] {"config", "create", "kill", "test"});
			case 1:
				switch(args[0])
				{
					case "config":
						return getListOfStringsMatchingLastWord(args, new String[] {"reload"});
					case "create":
						return getListOfStringsMatchingLastWord(args, new String[] {"ef#", "f#", "c#", "rain", "thunder", "lightning", "thunderstorm", "lightningstorm", "cell", "supercell", "severe", "severethunder", "severethunderstorm", "severelightning", "severelightningstorm", "cell", "supercell", "severe", "severethunder", "severethunderstorm", "severelightning", "severelightningstorm", "hail", "hailstorm", "sandstorm"});
					case "kill":
						return getListOfStringsMatchingLastWord(args, new String[] {"all"});
					case "test":
						return getListOfStringsMatchingLastWord(args, new String[] {"temperature", "humidity", "weather", "volcano"});
					default:
						return Collections.emptyList();
				}
			case 2:
				switch(args[0])
				{
					case "create":
						return getListOfStringsMatchingLastWord(args, new String[] {"~"});
					default:
						return Collections.emptyList();
				}
			case 3:
				switch(args[0])
				{
					case "create":
						return getListOfStringsMatchingLastWord(args, new String[] {"~"});
					default:
						return Collections.emptyList();
				}
			default: 
				switch(args[0])
				{
					case "create":
						return getListOfStringsMatchingLastWord(args, new String[] {"alwaysprogress", "isviolent", "violent", "isnatural", "natural", "isfirenado", "firenado", "neverdissipate", "neverdie"});
					default:
						return Collections.emptyList();
				}
		}
    }
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if (true)
		{
			execute2(server, sender, args);
			return;
		}
		/*
		World world = sender.getEntityWorld();
		Vec3d position = sender.getPositionVector();
		int dimension = world.provider.getDimension();
		
		try {
			if (args[0].equalsIgnoreCase("reload"))
			{
				WeatherUtilBlock.refreshTornadoList();
				WeatherUtilBlock.refreshCycloneList();
				notifyCommandListener(sender, this, "command.storm.reload.success");
				return;
			}
			else if (args[0].equalsIgnoreCase("volcano")&& position != Vec3d.ZERO) {
				if (dimension == 0) 
				{
					WeatherSystemServer wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
					VolcanoObject vo = new VolcanoObject(wm);
					vo.pos = new Vec3(position);
					vo.init();
					wm.addVolcanoObject(vo);
					vo.initPost();					
					wm.syncVolcanoNew(vo);
					
					notifyCommandListener(sender, this, "command.storm.volcano.success");
				}
				else
					notifyCommandListener(sender, this, "command.storm.reload.fail");
			} 
			else if (args[0].equalsIgnoreCase("lightning_bolt")) 
			{
				Random rand = new Random();
				EntityLightningBolt ent = new EntityLightningBolt(world, position.x + rand.nextInt(2) -  + rand.nextInt(2), position.y, position.z + rand.nextInt(2) -  + rand.nextInt(2));
				WeatherSystemServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
				wm.getWorld().weatherEffects.add(ent);
				wm.syncLightningNew(ent, false);
				notifyCommandListener(sender, this, "command.storm.lightning_bolt.success");
			} 
			if (args[0].equalsIgnoreCase("killAll")) {
				WeatherSystemServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
				notifyCommandListener(sender, this, "command.storm.kill.all");
				List<WeatherObject> listStorms = wm.getWeatherObjects();
				for (int i = 0; i < listStorms.size(); i++) {
					WeatherObject wo = listStorms.get(i);
					if (wo instanceof WeatherObject) {
						WeatherObject so = wo;
						Weather2.debug("force killing storm ID: " + so.id);
						so.setDead();
					}
				}
			} else if (args[0].equalsIgnoreCase("killDeadly")) {
				WeatherSystemServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
				notifyCommandListener(sender, this, "command.storm.kill.deadly");
				List<WeatherObject> listStorms = wm.getWeatherObjects();
				for (int i = 0; i < listStorms.size(); i++) {
					WeatherObject wo = listStorms.get(i);
					if (wo instanceof StormObject) {
						StormObject so = (StormObject)wo;
						if (so.stormStage > StormObject.Stage.THUNDER.getInt()) {
							Weather2.debug("force killing storm ID: " + so.id);
							so.setDead();
						}
					}
				}
			}
			else if (args[0].equalsIgnoreCase("killRain") || args[0].equalsIgnoreCase("killStorm")) {
				WeatherSystemServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
				notifyCommandListener(sender, this, "killing all raining or deadly storms");
				List<WeatherObject> listStorms = wm.getWeatherObjects();
				for (int i = 0; i < listStorms.size(); i++) {
					WeatherObject wo = listStorms.get(i);
					if (wo instanceof StormObject) {
						StormObject so = (StormObject)wo;
						if (so.stormStage >= StormObject.Stage.THUNDER.getInt() || so.isRaining) {
							Weather2.debug("force killing storm ID: " + so.id);
							so.setDead();
						}
					}
				}
			}
			else if (args.length > 0 && position != Vec3d.ZERO) {
				//TODO: make this handle non StormObject types better, currently makes instance and doesnt use that type if its a sandstorm
				boolean spawnCloudStorm = true;
				WeatherSystemServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
				StormObject so = new StormObject(wm);
				so.layer = 0; 
				so.player = sender instanceof EntityPlayer ? CoroUtilEntity.getName((EntityPlayer)sender) : CoroUtilEntity.getName(null);
				so.isNatural = false;
				so.stormTemperature = 0.1F;
				so.pos = new Vec3(position.x, StormObject.layers.get(so.layer), position.z);
				so.stormRain = 10.0F;
				so.isRaining = true;
					
				if (!args[0].equals("rain"))
					so.initRealStorm(null, null);
					
				if (args[0].equals("rain")) {}
				else if (args[0].equalsIgnoreCase("thunder") || args[0].equalsIgnoreCase("lightning"))
				{
					so.stormRain = 30.0F;
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 1;
				} else if (args[0].equalsIgnoreCase("supercell")) {
					so.stormRain = 55.0F;
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 2;
				} else if (args[0].equalsIgnoreCase("spout")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 2;
					so.isSpout = true;
				} else if (args[0].equalsIgnoreCase("hail")) {
					so.stormRain = 85.0F;
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 3;
				} else if (args[0].equalsIgnoreCase("f5")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 9;
				} else if (args[0].equalsIgnoreCase("f4")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 8;
				} else if (args[0].equalsIgnoreCase("f3")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 7;
				} else if (args[0].equalsIgnoreCase("f2")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 6;
				} else if (args[0].equalsIgnoreCase("f1")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 5;
				} else if (args[0].equalsIgnoreCase("f0")) {
					so.stormType = StormObject.Type.LAND.getInt();
					so.stormStage = 4;
				} else if (args[0].equalsIgnoreCase("c0")) {
					so.stormType = StormObject.Type.WATER.getInt();
					so.stormStage = 4;
				} else if (args[0].equalsIgnoreCase("c1")) {
					so.stormType = StormObject.Type.WATER.getInt();
					so.stormStage = 5;
				} else if (args[0].equalsIgnoreCase("c2")) {
					so.stormType = StormObject.Type.WATER.getInt();
					so.stormStage = 6;
				} else if (args[0].equalsIgnoreCase("c3")) {
					so.stormType = StormObject.Type.WATER.getInt();
					so.stormStage = 7;
				} else if (args[0].equalsIgnoreCase("c4")) {
					so.stormType = StormObject.Type.WATER.getInt();
					so.stormStage = 8;
				} else if (args[0].equalsIgnoreCase("c5")) {
					so.stormType = StormObject.Type.WATER.getInt();
					so.stormStage = 9;
				} else if (args[0].equalsIgnoreCase("sandstorm")) {		
					SandstormObject sandstorm = new SandstormObject(wm);
					Vec3 pos = new Vec3(position.x, world.getHeight(new BlockPos(position.x, 0, position.z)).getY() + 1, position.z);
	
					/**
					 * adjust position upwind 150 blocks
					 */
					/*float angle = wm.getWindManager().getWindAngleForClouds();
					double vecX = -Math.sin(Math.toRadians(angle));
					double vecZ = Math.cos(Math.toRadians(angle));
					double speed = 150D;
					pos.xCoord -= vecX * speed;
					pos.zCoord -= vecZ * speed;
					
					sandstorm.init();
					sandstorm.initSandstormSpawn(pos);
						
					wm.addStormObject(sandstorm);
					wm.syncStormNew(sandstorm);
					spawnCloudStorm = false;
					wm.windMan.startHighWindEvent();
					wm.windMan.lowWindTimer = 0;
					
				} else if (args[0].equalsIgnoreCase("sandstorm")) {
					boolean spawned = wm.spawnSandstorm(world, new Vec3(position));
					spawnCloudStorm = false;
					if (!spawned) {
						notifyCommandListener(sender, this, "couldnt find spot to spawn");
						return;
					} else {
						wm.windMan.startHighWindEvent();
						wm.windMan.lowWindTimer = 0;
					}
				}
					
				boolean posChange = false;
				double posX = position.x;
				double posZ = position.z;
				
				if (args.length > 1) 
				{
					if (!args[1].contains("~"))
						{posX = Double.parseDouble(args[1]); posChange = true;}
					else if (args[1].contains("~"))
						{posX = position.x; posChange = true;}
				}
				if (args.length > 2) 
				{
					if (!args[2].contains("~"))
						{posZ = Double.parseDouble(args[2]); posChange = true;}
					else if (args[2].contains("~"))
						{posZ = position.z; posChange = true;}
				}
				if (posChange) {so.pos = new Vec3(posX, StormObject.layers.get(so.layer), posZ);}
				if (args.length > 3 && Float.parseFloat(args[3]) > -1.0F) {so.setSpeed(Float.parseFloat(args[3]));}
				if (args.length > 4 && Float.parseFloat(args[4]) > -1.0F) {so.setAngle(Float.parseFloat(args[4]));}
				
				String flags = "";
					
				if (args.length > 5) {
					for(int i = 5; i < args.length;i++)
					{
						if (args[i].equalsIgnoreCase("alwaysprogress")) 
						{
							so.alwaysProgresses = true;
							if (flags == "")
								flags = "alwaysProgress";
							else
								flags = flags + ", alwaysProgress";
						}
						else if (args[i].equalsIgnoreCase("neverdissipate")) 
						{
							so.neverDissipate = true;
							if (flags == "")
								flags = "neverDissipate";
							else
								flags = flags + ", neverDissipate";
						}
						else if (args[i].equalsIgnoreCase("firenado")) 
						{
							so.isFirenado = true;
							if (flags == "")
								flags = "firenado";
							else
								flags = flags + ", firenado";
						}
						else if (args[i].equalsIgnoreCase("violent")) 
						{
							so.isViolent = true;
							if (flags == "")
								flags = "violent";
							else
								flags = flags + ", violent";
						}else if (args[i].equalsIgnoreCase("real")) 
						{
							so.isNatural = true;
							if (flags == "")
								flags = "real";
							else
								flags = flags + ", real";
						}
					}
				}
					
					if (spawnCloudStorm) {
					
				so.init();
				if (so.isNatural)
					so.initRealStorm(null, null);
				else
					so.stormStageMax = so.stormStage;
						
				wm.addStormObject(so);
				wm.syncStormNew(so);
					}
					notifyCommandListener(sender, this, "Storm " + args[0] + " spawned! Flags: " + flags);
				}
				else if (args[0].equals("help")) {
					notifyCommandListener(sender, this, getUsage(sender));
				} 
				else if (args[0].equals("wind")) {
					if (args[0].equals("high")) {
				boolean doHighOn = false;
				boolean doHighOff = false;
				if (args.length > 2) {
					 if (args[1].equals("start")) {
						 doHighOn = true;
					 } else if (args[1].equals("stop")) {
						 doHighOff = true;
					 }
				} else {
					doHighOn = true;
				}
				WeatherSystemServer wm = ServerTickHandler.getWeatherSystemForDim(dimension);
				if (doHighOn) {
					wm.windMan.startHighWindEvent();
					//cancel any low wind state if there is one
					wm.windMan.lowWindTimer = 0;
					notifyCommandListener(sender, this, "started high wind event");
				} else if (doHighOff) {
					wm.windMan.stopHighWindEvent();
					notifyCommandListener(sender, this, "stopped high wind event");
				}
					} else if (args[0].equals("low")) {
				boolean doLowOn = false;
				boolean doLowOff = false;
				if (args.length > 2) {
					 if (args[1].equals("start")) {
						 doLowOn = true;
					 } else if (args[1].equals("stop")) {
						 doLowOff = true;
					 }
				} else {
					doLowOn = true;
				}
				WeatherSystemServer wm = ServerTickHandler.getWeatherSystemForDim(dimension);
				if (doLowOn) {
					wm.windMan.startLowWindEvent();
					//cancel any high wind state if there is one
					wm.windMan.highWindTimer = 0;
					notifyCommandListener(sender, this, "started low wind event");
				} else if (doLowOff) {
					wm.windMan.stopLowWindEvent();
					notifyCommandListener(sender, this, "stopped low wind event");
				}
					}
				}
				else
					notifyCommandListener(sender, this, getUsage(sender));
		} catch (Exception ex) {
			System.out.println("Exception handling Weather2 command");
			notifyCommandListener(sender, this, getUsage(sender));
			ex.printStackTrace();
		}
		*/
	}
}
