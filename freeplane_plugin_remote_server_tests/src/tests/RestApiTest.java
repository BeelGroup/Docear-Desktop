//package tests;
//import static org.fest.assertions.Assertions.assertThat;
//
//import java.io.File;
//import java.io.InputStream;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.concurrent.Semaphore;
//
//import javax.ws.rs.core.MediaType;
//
//import org.freeplane.plugin.webservice.v10.Webservice;
//import org.freeplane.plugin.webservice.v10.model.MapModel;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.config.ClientConfig;
//import com.sun.jersey.api.client.config.DefaultClientConfig;
//import com.sun.jersey.api.json.JSONConfiguration;
//
//
//public class RestApiTest {
//
//	private static final String hostAddress = "http://localhost:8081/rest/v1";
//
//	private Client client;
//	private WebResource baseResource;
//
//
//
//
//	@Before
//	public void setUp() throws Exception {
//		ClientConfig clientConfig = new DefaultClientConfig();
//		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//
//		client = Client.create();
//		baseResource = client.resource(hostAddress);
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		client.destroy();
//	}
//
//	@Test
//	public void getMapAsJsonTest() throws URISyntaxException {
//		sendMindMapToServer(1);
//
//		WebResource getMapResource = baseResource.path("map").path("1").path("json").queryParam("nodeCount", "5");
//		ClientResponse cr = getMapResource.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
//
//
//
//		// Jackson Way to get map
//		//		String content = cr.getEntity(String.class);
//		//		System.out.println(content);
//		//		ObjectMapper mapper = new ObjectMapper();
//		//		mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
//		//      MapModel map = mapper.readValue(content, MapModel.class);// cr.getEntity(MapModel.class);
//
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		MapModel map = cr.getEntity(MapModel.class);
//
//		assertThat(map).isInstanceOf(MapModel.class);
//		assertThat(map.root.nodeText).isEqualTo("foo2");
//
//		closeMindMapOnServer(1);
//	}
//
//	//	@Test
//	//	public void getAddAndRemoveNodeToFirstRightChild() {
//	//		WebResource wr = client.resource("http://localhost:8080/rest/v1");
//	//		
//	//		MapModel map = wr.path("map").path("json").path("1").queryParam("nodeCount", "5").get(MapModel.class);
//	//		DefaultNodeModel node =  map.root.rightChildren.get(0);
//	//		
//	//		WebResource addNodeResource = wr.path("node").path(node.id).queryParam("nodeText", "myNode");
//	//		ClientResponse cr = addNodeResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class);
//	//		
//	//		assertThat(cr.getStatus()).isEqualTo(200);
//	//		DefaultNodeModel newNode = cr.getEntity(DefaultNodeModel.class);
//	//		
//	//		assertThat(newNode).isInstanceOf(DefaultNodeModel.class);
//	//		assertThat(newNode.nodeText).isEqualTo("myNode");
//	//		
//	//		
//	//		
//	//		Boolean deleted = Boolean.valueOf(wr.path("removeNode").path(newNode.id).delete(String.class));
//	//		assertThat(deleted).isTrue();
//	//		
//	//	}
//
//	@Test
//	public void changeNode() throws URISyntaxException {
//		ClientResponse cr;
//		sendMindMapToServer(5);
//		//String entity = "{\"username\":\"Jon Doe\"}";
//		String entity = "Jon Doe";
//
//		cr = requestLock(5+"", "ID_1", entity);
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		
//		WebResource sendNode = baseResource.path("map/5/node");
//		String httpBody = "{\"id\":\"ID_1\",\"nodeText\":\"This is the new NodeText\"}";
//
//		cr = sendNode.type(MediaType.APPLICATION_JSON_TYPE).entity(httpBody).put(ClientResponse.class);
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		cr = releaseLock(5+"", "ID_1");
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		closeMindMapOnServer(5);
//	}
//
//	@Test
//	public void simulateMultipleUser() {
//		final Semaphore finishSemaphore = new Semaphore(-3);
//		for(int i = 1; i <= 4; i++) {
//			final int mapId = i == 4 ? 5 : i;
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					sendMindMapToServer(mapId);
//					
//					
//					WebResource getMapResource = baseResource.path("map").path(mapId+"").path("json").queryParam("nodeCount", "5");
//					ClientResponse cr = getMapResource.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
//					assertThat(cr.getStatus()).isEqualTo(200);
//					
//					closeMindMapOnServer(mapId);
//					
//					finishSemaphore.release();
//				}
//			}).start();
//		}
//		
//		finishSemaphore.acquireUninterruptibly();
//	}
//	@Test
//	public void requestLockTwoTimes() throws URISyntaxException {
//		ClientResponse cr;
//
//		sendMindMapToServer(5);
//
//		String name1 = "Jon Doe";
//		String name2 = "Jonathan";
//
//		//Jon Doe gets lock
//		cr = requestLock(5+"", "ID_1", name1);
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		//request with other user, should fail
//		cr = requestLock(5+"", "ID_1", name2);
//		assertThat(cr.getStatus()).isEqualTo(403);
//
//		//Jon Does second request, should still work
//		cr = requestLock(5+"", "ID_1", name1);
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		closeMindMapOnServer(5);
//	}
//
//	@Test
//	public void LockThenUnlockThenLockWithOtherUser() throws URISyntaxException {
//		ClientResponse cr;
//		sendMindMapToServer(5);
//
//		String name1 = "Jon Doe";
//		String name2 = "Jonathan";
//
//		//Jon Doe gets lock
//		cr = requestLock(5+"", "ID_1", name1);
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		//Jon releases lock, should work
//		releaseLock(5+"", "ID_1");
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		//Jonathan request, should get
//		requestLock(5+"", "ID_1", name2);
//		assertThat(cr.getStatus()).isEqualTo(200);
//
//		closeMindMapOnServer(5);
//	}
//
//	@Test
//	public void sendMindmapToApplicationAndGetMindmapAsJson() throws URISyntaxException {
//		sendMindMapToServer(1);
//		//		String nodeId = wr.path("addNodeToRootNode").accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
//		//		assertThat(nodeId).startsWith("ID_");
//		//		
//		MapModel model = baseResource.path("map/1/json").accept(MediaType.APPLICATION_JSON_TYPE).get(MapModel.class);
//		assertThat(model).isNotNull();
//		assertThat(model.root.nodeText).isEqualTo("foo2");
//
//		closeMindMapOnServer(1);
//	}
//
//	@Test
//	public void lockNodeAndRefreshLockAndLetReleaseDueToExpiration() {
//		sendMindMapToServer(5);
//
//
//
//		closeMindMapOnServer(5);
//	}
//
//	public void sendMindMapToServer(int id) {
//		WebResource sendMapResource = baseResource.path("map");
//		InputStream in = Webservice.class.getResourceAsStream("/files/mindmaps/"+id+".mm");
//		URL pathURL = Webservice.class.getResource("/files/mindmaps/"+id+".mm");
//		File f = null;
//		try {
//			f = new File(pathURL.toURI());
//		} catch (URISyntaxException e) {}
//
//
//		String contentDeposition = "attachement; filename=\""+id+".mm\"";
//		assertThat(f).isNotNull();
//
//		//		MultivaluedMap formData = new MultivaluedMapImpl();
//		//		formData.add("file", in);
//		ClientResponse response = sendMapResource.type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
//				.header("Content-Deposition", contentDeposition)
//				.put(ClientResponse.class, in);
//
//		assertThat(response.getStatus()).isEqualTo(200);
//	}
//
//	public void closeMindMapOnServer(int id) {
//		WebResource closeMap = baseResource.path("/map/"+id);
//		ClientResponse cr = closeMap.delete(ClientResponse.class);
//		assertThat(cr.getStatus()).isEqualTo(200);
//	}
//
//	public ClientResponse requestLock(String mapId, String nodeId, String username) {
//		WebResource requestLock = baseResource.path("map/5/node/ID_1/requestLock");
//		ClientResponse cr = requestLock.type(MediaType.APPLICATION_JSON_TYPE).entity(username).put(ClientResponse.class);
//		return cr;
//	}
//
//	public ClientResponse releaseLock(String mapId, String nodeId) {
//		WebResource releaseLock = baseResource.path("map/"+mapId+"/node/"+nodeId+"/releaseLock");
//		ClientResponse cr = releaseLock.delete(ClientResponse.class);
//		return cr;
//	}
//
//	//	@Test
//	//	public void selectOpenedMapTest() throws URISyntaxException {
//	//		WebResource wr = client.resource("http://localhost:8080/rest/v1");
//	//		
//	//		// 1. send a map
//	//		WebResource sendMapResource = wr.path("openMindmap");
//	//		InputStream in = Webservice.class.getResourceAsStream("/files/mindmaps/2.mm");
//	//		URL pathURL = Webservice.class.getResource("/files/mindmaps/2.mm");
//	//		File f = new File( pathURL.toURI());
//	//		
//	//		String mapId = WebserviceHelper.getMapIdFromFile(f);
//	//		
//	//		String contentDeposition = "attachement; filename=\"2.mm\"";
//	//		assertThat(f).isNotNull();
//	//		
//	//		ClientResponse response = sendMapResource.type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
//	//				.header("Content-Deposition", contentDeposition)
//	//				.put(ClientResponse.class, in);
//	//		
//	//		assertThat(response.getStatus()).isEqualTo(200);		
//	//		
//	//		// 2. select another map
//	//		response = wr.path("map").path("json").path("1").get(ClientResponse.class);
//	//		assertThat(response.getStatus()).isEqualTo(200);
//	//		
//	//		// 3. reselect first map
//	//		response = wr.path("selectMindmap").path(mapId).put(ClientResponse.class);
//	//		assertThat(response.getStatus()).isEqualTo(200);
//	//		
//	//		DefaultNodeModel node = wr.path("node").path("ID_1247776391").get(DefaultNodeModel.class);
//	//		assertThat(node.nodeText).isEqualTo("test");
//	//		
//	//	}
//
//}