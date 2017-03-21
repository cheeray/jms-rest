package com.cheeray.rest.config;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: YAML configuration ...
 * 
 * @author Chengwei.Yan
 * 
 */
public class Config {
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	public static BridgeConfig parse(File f) throws IOException {
		LOG.debug("Parsing YAML configuration: {}.", f.getPath());
		return SimpleYaml.parse(f, BridgeConfig.class);
		// FIXME: Snakeyaml doesn't work ...

		/*
		 * try (FileInputStream fi = new FileInputStream(f)) {
		 * BridgeConfigConstructor constructor = new BridgeConfigConstructor();
		 * MissingPropertiesChecker propertiesChecker = new
		 * MissingPropertiesChecker();
		 * constructor.setPropertyUtils(propertiesChecker); final Yaml yaml =
		 * new Yaml(constructor); return yaml.loadAs(fi, BridgeConfig.class); }
		 * catch (IOException e) { throw e; }
		 */
	}

	/*
	 * private static class MissingPropertiesChecker extends PropertyUtils {
	 * private final Set<String> missingProperties = new HashSet<>();
	 * 
	 * public MissingPropertiesChecker() { setSkipMissingProperties(true); }
	 * 
	 * @Override public Property getProperty(Class<? extends Object> type,
	 * String name) throws IntrospectionException { Property result =
	 * super.getProperty(type, name); if (result instanceof MissingProperty) {
	 * missingProperties.add(result.getName()); } return result; }
	 * 
	 * public void check() throws ConfigurationException { if
	 * (!missingProperties.isEmpty()) { throw new ConfigurationException(
	 * "Invalid yaml. Please remove properties " + missingProperties +
	 * " from your cassandra.yaml"); } } }
	 * 
	 * static class BridgeConfigConstructor extends Constructor { private final
	 * BridgeConfigConstruct bridgeConfigConstruct; private final
	 * JmsConfigConstruct JmsConfigConstruct; private RestConfigConstruct
	 * restConfigConstruct;
	 * 
	 * BridgeConfigConstructor() { this.bridgeConfigConstruct = new
	 * BridgeConfigConstruct(); this.JmsConfigConstruct = new
	 * JmsConfigConstruct(); this.restConfigConstruct = new
	 * RestConfigConstruct(); yamlMultiConstructors.put(
	 * "tag:yaml.org,2002:com.cheeray.rest.yml.BridgeConfig",
	 * bridgeConfigConstruct); yamlMultiConstructors.put(
	 * "tag:yaml.org,2002:com.cheeray.rest.yml.JmsConfig", JmsConfigConstruct);
	 * yamlMultiConstructors.put(
	 * "tag:yaml.org,2002:com.cheeray.rest.yml.RestConfig",
	 * restConfigConstruct); }
	 * 
	 * class BridgeConfigConstruct extends Constructor.ConstructMapping {
	 * 
	 * @Override protected Object constructJavaBean2ndStep(MappingNode node,
	 * Object object) { if (node.getTag().matches(BridgeConfig.class)) {
	 * Map<String, Object> map = new HashMap<>(); for (NodeTuple nt :
	 * node.getValue()) { ScalarNode sn = (ScalarNode) nt.getKeyNode();
	 * ScalarNode vn = (ScalarNode) nt.getValueNode(); switch (sn.getValue()) {
	 * case "inbound": case "outbound": { Map<String, String> kvs = new
	 * HashMap<>(); for (String tp : vn.getValue().split(" ")) { String[] kv =
	 * tp.split(":"); if (kv.length == 2) { kvs.put(kv[0], kv[1]); } }
	 * map.put(sn.getValue(), new JmsConfig((String) kvs.get("host"),
	 * Integer.parseInt(kvs.get("port") .toString()), (String) kvs
	 * .get("channel"), (String) kvs.get("manager"), (String)
	 * kvs.get("queues"))); } break; case "local": case "remote": { Map<String,
	 * String> kvs = new HashMap<>(); for (String tp : vn.getValue().split(" "))
	 * { String[] kv = tp.split(":"); if (kv.length == 2) { kvs.put(kv[0],
	 * kv[1]); } } map.put(sn.getValue(), new RestConfig((String)
	 * map.get("scheme"), (String) map.get("server"), Integer
	 * .parseInt(map.get("port") .toString()), (String) map.get("context"),
	 * (String) map.get("userName"), (String) map.get("password"))); } break; }
	 * } return new BridgeConfig((JmsConfig) map.get("inbound"), (JmsConfig)
	 * map.get("outbound"), (RestConfig) map.get("local"), (RestConfig)
	 * map.get("remote")); } else { // create JavaBean return
	 * super.constructJavaBean2ndStep(node, object); } } }
	 * 
	 * class JmsConfigConstruct extends Constructor.ConstructMapping {
	 * 
	 * @Override protected Object constructJavaBean2ndStep(MappingNode node,
	 * Object object) { if (node.getTag().matches(JmsConfig.class)) { // create
	 * a map Map<?, ?> map = constructMapping(node); return new
	 * JmsConfig((String) map.get("host"),
	 * Integer.parseInt(map.get("port").toString()), (String)
	 * map.get("channel"), (String) map.get("manager"), (String)
	 * map.get("queues")); } else { // create JavaBean return
	 * super.constructJavaBean2ndStep(node, object); } } }
	 * 
	 * class RestConfigConstruct extends Constructor.ConstructMapping {
	 * 
	 * @Override protected Object constructJavaBean2ndStep(MappingNode node,
	 * Object object) { if (node.getTag().matches(RestConfig.class)) { // create
	 * a map Map<?, ?> map = constructMapping(node); return new
	 * RestConfig((String) map.get("scheme"), (String) map.get("server"),
	 * Integer.parseInt(map .get("port").toString()), (String)
	 * map.get("context"), (String) map.get("userName"), (String)
	 * map.get("password")); } else { // create JavaBean return
	 * super.constructJavaBean2ndStep(node, object); } } } }
	 */

}
