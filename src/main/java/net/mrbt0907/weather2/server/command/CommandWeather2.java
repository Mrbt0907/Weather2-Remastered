package net.mrbt0907.weather2.server.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.network.packets.PacketSound;
import net.mrbt0907.weather2.network.packets.PacketVolcanoObject;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.ReflectionHelper;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.StormObject.StormType;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

public class CommandWeather2 extends CommandBase
{	
	@Override
	public String getName()
	{
		return "storm";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "command.storm.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		switch(args.length - 1)
		{
			case 0:
				return getListOfStringsMatchingLastWord(args, new String[] {"config", "create", "kill", "view", "test"});
			case 1:
				switch(args[0])
				{
					case "config":
						return getListOfStringsMatchingLastWord(args, new String[] {"refresh"});
					case "create":
						return getListOfStringsMatchingLastWord(args, new String[] {"random", "clouds", "rainstorm", "thunderstorm", "supercell", "tropicaldisturbance", "tropicaldepression" , "tropicalstorm", "sandstorm", "ef#", "f#", "c#"});
					case "kill":
						return getListOfStringsMatchingLastWord(args, new String[] {"all", "particles"});
					case "test":
						return getListOfStringsMatchingLastWord(args, new String[] {"class", "volcano"});
					default:
						return Collections.emptyList();
				}
			case 2:
				switch(args[0])
				{
					case "config":
						return getListOfStringsMatchingLastWord(args, new String[] {"all", "dimensionlist", "grablist", "replacelist", "stagelist", "windlist", "sounds"});
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
						return getListOfStringsMatchingLastWord(args, new String[] {"alwaysprogress", "ishailing", "isviolent", "isnatural", "isfirenado", "neverdissipate", "dontconvert", "revives=#", "direction=<#/north/south/east/west>", "speed=#", "size=#", "name=<Word>"});
					default:
						return Collections.emptyList();
				}
		}
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		World world = sender.getEntityWorld();
		Vec3d pos = sender.getPositionVector();
		int size = args.length;
		
		if (size > 0)
			switch(args[0].toLowerCase())
			{
				case "config":
					if (size > 1)
						switch (args[1].toLowerCase())
						{
							case "refresh":
								if (size > 2)
									switch (args[2].toLowerCase())
									{
										case "all":
											WeatherAPI.refreshDimensionRules();
											WeatherAPI.refreshGrabRules();
											PacketSound.reset((EntityPlayerMP)sender);
											say(sender, "config.refresh.all.success");
											break;
										case "dimensionlist":
											WeatherAPI.refreshDimensionRules();
											say(sender, "config.refresh.dimensionlist.success");
											break;
										case "grablist":
											WeatherAPI.refreshGrabRules();
											say(sender, "config.refresh.grablist.success");
											break;
										case "replacelist":
											WeatherAPI.refreshGrabRules();
											say(sender, "config.refresh.replacelist.success");
											break;
										case "stagelist":
											WeatherAPI.refreshStages();
											say(sender, "config.refresh.stagelist.success");
											break;
										case "windlist":
											WeatherAPI.refreshGrabRules();
											say(sender, "config.refresh.windlist.success");
											break;
										case "sounds": case "sound":
											if (sender instanceof EntityPlayerMP)
											{
												PacketSound.reset((EntityPlayerMP)sender);
												say(sender, "config.refresh.sounds.success");
											}
											else
												say(sender, "config.refresh.sounds.fail");
											break;
										default:
											say(sender, "config.refresh.usage");
									}
								else
									say(sender, "config.refresh.usage");
								break;
							default:
								say(sender, "config.usage");
						}
					else
						say(sender, "config.usage");
					break;
				case "create":
					if (size > 1)
					{
						int stage = -1;
						boolean isRaining = false, isSandstorm = false, isCyclone = false , isRandom = false;
						String type = args[1].toLowerCase(); 
						
						switch (type)
						{
							case "random":
								isRandom = true;
								stage = Stage.NORMAL.getStage();
								break;
							case "cloud": case "clouds":
								stage = Stage.NORMAL.getStage();
								break;
							case "rain": case "rainstorm":
								isRaining = true;
								stage = Stage.RAIN.getStage();
								break;
							case "thunder": case "thunderstorm": case "lightning": case "lightningstorm":
								isRaining = true;
								stage = Stage.THUNDER.getStage();
								break;
							case "supercell": case "cell": case "severe": case "severethunder": case "severethunderstorm": case "severelightning": case "severelightningstorm":
								isRaining = true;
								stage = Stage.SEVERE.getStage();
								break;
							case "tropicaldisturbance": case "td1":
								isRaining = true;
								isCyclone = true;
								stage = Stage.TROPICAL_DISTURBANCE.getStage();
								break;
							case "tropicaldepression": case "td2":
								isRaining = true;
								isCyclone = true;
								stage = Stage.TROPICAL_DEPRESSION.getStage();
								break;
							case "tropicalstorm": case "ts":
								isRaining = true;
								isCyclone = true;
								stage = Stage.TROPICAL_STORM.getStage();
								break;
							case "sandstorm":
								isSandstorm = true;
								stage = Stage.NORMAL.getStage();
								break;
							default:
								isRaining = true;
								
								if (type.matches("(ef|f)\\d+"))
									stage = Stage.TORNADO.getStage() + Integer.parseInt(type.replaceAll("\\D*", ""));
								else if (type.matches("(category|c)\\d+"))
								{
									isCyclone = true;
									stage = Stage.TROPICAL_STORM.getStage() + Integer.parseInt(type.replaceAll("\\D*", ""));
								}
						}
						
						if (stage > -1)
						{
							if (isSandstorm)
							{
								int dimension = world.provider.getDimension();
								
								WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dimension);
								if (size > 3)
								{
									BlockPos temp;
									try
									{
										temp = parseBlockPos2(sender, args, 2, true);
									}
									catch (NumberInvalidException e)
									{
										Weather2.error(e);
										say(sender, "create.sandstorm.fail.a");
										return;
									}
									pos = new Vec3d(temp.getX(), 0, temp.getY());
								}
								boolean spawned = wm.spawnSandstorm(new Vec3(pos));
								
								if (!spawned)
									say(sender, "create.sandstorm.fail.b");
								else
									say(sender, "create.sandstorm.success", Math.round(pos.x), Math.round(pos.z));
							}
							else
							{
								boolean isViolent = false, isNatural = false, isFirenado = false, neverDissipate = false, shouldConvert = true, alwaysProgress = false, isHailing = false;//, shouldaim = false;
								float sizeMultiplier = -1.0F, angle = -1.0F, speed = -1.0F;
								int revives = -1, dimension = world.provider.getDimension();
								String flag, flags = "", name = "";
								
								if (size > 3)
									try
									{
										BlockPos temp = parseBlockPos2(sender, args, 2, true);
										pos = new Vec3d(temp.getX(), temp.getY(), temp.getZ());
									}
									catch (NumberInvalidException e)
									{
										Weather2.error(e);
										say(sender, "create.fail");
										return;
									}
								
								for (int i = 4; i < size;i++)
								{
									flag = args[i].toLowerCase();
									switch(flag)
									{
										case "alwaysprogress":
											if (!alwaysProgress)
											{
												alwaysProgress = true;
												flags += ", Always Progresses";
											}
											break;
										case "isviolent": case "violent":
											if (!isViolent)
											{
												isViolent = true;
												flags += ", Violent Storm";
											}
											break;
										case "isnatural": case "natural":
											if (!isNatural)
											{
												isNatural = true;
												flags += ", Starts Naturally";
											}
											break;
										case "isfirenado": case "firenado":
											if (!isFirenado)
											{
												isFirenado = true;
												flags += ", Is A Firenado";
											}
											break;
										case "neverdissipate": case "neverdie":
											if (!neverDissipate)
											{
												neverDissipate = true;
												flags += ", Never Dissipates";
											}
											break;
										case "dontconvert": case "noconvert": case "convert":
											if (shouldConvert)
											{
												shouldConvert = false;
												flags += ", Never Converts To Hurricane";
											}
											break;
										case "ishailing": case "hailing": case "hail":
											if (!isHailing)
											{
												isHailing = true;
												flags += ", Storm Is Hailing";
											}
										default:
											if (flag.matches("revives\\=\\d+"))
											{
												if (revives < 0)
												{
													revives = Integer.parseInt(flag.replaceAll("\\D*", ""));
													flags += ", Will Revive " + revives + " time" + (revives > 1 ? "s" : "");
												}
											}
											else if (flag.matches("(angle|direction)\\=(north|south|east|west|\\d+)"))
											{
												if (angle < 0.0F)
												{
													angle = flag.contains("north") ? 180.0F : flag.contains("east") ? 270.0F : flag.contains("south") ? 0.0F : flag.contains("west") ? 90.0F : Float.parseFloat(flag.replaceAll("[^\\d\\.]*", ""));
													flags += ", Aiming at " + angle + " degrees";
												}
											}
											else if (flag.matches("speed\\=[\\d\\.]+"))
											{
												if (speed < 0.0F)
												{
													speed = Float.parseFloat(flag.replaceAll("[^\\d\\.]*", ""));
													flags += ", Moving At " + speed + " M/s";
												}
											}
											else if (flag.matches("size\\=[\\d\\.\\%]+"))
											{
												if (sizeMultiplier < 0.0F)
												{
													sizeMultiplier = Float.parseFloat(flag.replaceAll("[^\\d\\.]*", "")) * 0.01F;
													
													flags += ", Will Grow " + (sizeMultiplier * 100.0F) + "%" + (sizeMultiplier < 1.0F ? " Smaller Than Normal" : sizeMultiplier == 1.0F ? "" : "Larger Than Normal");
												}
											}
											else if (flag.matches("name\\=\\w+"))
											{
												if (name == "")
												{
													name = args[i].replaceFirst("[nN][aA][mM][eE]\\=", "");
													flags += ", Named " + name;
												}
											}
										
										
									}
								}
								
								//TODO: make this handle non StormObject types better, currently makes instance and doesnt use that type if its a sandstorm
								WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dimension);
								StormObject so = new StormObject(wm.getGlobalFront());
								
								so.layer = 0;
								so.isNatural = isNatural;
								so.stormTemperature = 0.1F;
								so.pos = new Vec3(pos.x, so.getLayerHeight(), pos.z);
								so.stormStage = isRandom ? so.rollDiceOnMaxIntensity() : stage;
								so.stormStageMax = so.stormStage;
								so.stormIntensity = so.stormStage - 0.99F;
								so.stormHumidity = isRaining ? (isNatural ? 50.0F : isHailing ? 200.0F : so.stormStage * 50.0F) + 1.0F : 0.0F;
								so.stormSizeRate = sizeMultiplier;
								so.overrideAngle = angle >= 0.0F;
								so.overrideNewAngle = angle;
								so.overrideMotion = speed >= 0.0F;
								so.overrideNewMotion = speed;
								so.alwaysProgresses = alwaysProgress;
								so.neverDissipate = neverDissipate;
								so.isFirenado = isFirenado;
								so.shouldConvert = shouldConvert;
								so.isViolent = isViolent;
								so.maxRevives = revives;
								so.stormName = name;
								so.shouldBuildHumidity = true;
								
								if (isCyclone || isRandom && so.stormStage > 3 && Maths.chance(25))
									so.stormType = StormType.WATER.ordinal();
								
								so.init();
									
								if (so.stormHumidity > 0.0F && so.isNatural)
									so.initRealStorm();
								else
								{
									so.canProgress = true;
									
									if (so.stormSizeRate < 0.0F)
										so.stormSizeRate = (float) Maths.random(ConfigStorm.min_size_growth, ConfigStorm.max_size_growth);
									
									if (so.isViolent)
									{
										so.stormSizeRate += Maths.random(ConfigStorm.min_violent_size_growth, ConfigStorm.max_violent_size_growth);
										if (so.stormStageMax < 9)
											so.stormStageMax += 1;
									}
								}

								so.updateType();
								
								wm.getGlobalFront().addWeatherObject(so);
								PacketWeatherObject.create(wm.getDimension(), so);
								
								say(sender, "create.success", so.getName(), Math.round(pos.x), Math.round(pos.z), flags);
								return;
							}
						}
						else
							say(sender, "create.usage");
					}
					else
						say(sender, "create.usage");
					break;
				case "kill":
					if (size > 1)
						switch (args[1].toLowerCase())
						{
							case "all":
								WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
								List<FrontObject> fronts = wm.getFronts();
								size = wm.getWeatherObjects().size();
								if (size > 0)
								{
									for (int i = 0; i < fronts.size(); i++)
									{
										FrontObject front = fronts.get(i);
										Weather2.debug("Killing front " + front.getUUID());
										front.isDead = true;
									}
									say(sender, "kill.all.success", size);
								}
								else
									say(sender, "kill.all.fail");
								break;
							case "particle": case "particles":
								if (sender instanceof EntityPlayerMP)
								{
									PacketWeatherObject.clientCleanup((EntityPlayerMP) sender);
									say(sender, "kill.particles.success");
								}
								else
									say(sender, "kill.particles.fail");
								break;
							default:
								say(sender, "kill.usage");
						}
					else
						say(sender, "kill.usage");
					break;
				case "view":
					if (size > 1)
						switch (args[1].toLowerCase())
						{
							default:
								say(sender, "view.fail");
						}
					else
						say(sender, "view.fail");	
					break;
				case "test":
					if (size > 1)
						switch (args[1].toLowerCase())
						{
							case "volcano":
							{
								WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(0);
								VolcanoObject vo = new VolcanoObject(wm);
								vo.pos = new CoroUtil.util.Vec3(pos);
								vo.init();
								wm.addVolcanoObject(vo);				
								PacketVolcanoObject.create(wm.getDimension(), vo);
								
								say(sender, "test.volcano.success");
								break;
							}
							case "class":
							{
								if (size > 2)
								{
									List<String> found = ReflectionHelper.view(args[2]);
									for (String line : found)
										say(sender, "test.class.success", line);
								}
								else
									say(sender, "test.class.usage");
								break;
							}
							default:
								say(sender, "test.usage");
						}
					else
						say(sender, "test.usage");	
					break;
				default:
					say(sender, "usage");
			}
		else
			say(sender, "usage");
	}
	
	public static BlockPos parseBlockPos2(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
	{
		BlockPos blockpos = sender.getPosition();
		return new BlockPos(parseDouble((double)blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), 0.0D, parseDouble((double)blockpos.getZ(), args[startIndex + 1], -30000000, 30000000, centerBlock));
	}
		
	private void say(ICommandSender sender, String localizationID, Object... args)
	{
		notifyCommandListener(sender, this, "command." + getName() + "." + localizationID, args);
	}
}
