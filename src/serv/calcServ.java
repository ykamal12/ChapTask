package serv;


	import modell.calcu;
	import javax.ejb.Stateless;
	import javax.persistence.EntityManager;
	import javax.persistence.PersistenceContext;
	import javax.ws.rs.*;
	import javax.ws.rs.core.MediaType;
	import javax.ws.rs.core.Response;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	import java.util.function.BinaryOperator;

	@Stateless
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	
	public class calcServ {

	    @PersistenceContext(unitName = "calc")
	    private EntityManager entityManager;

	    private static final Map<String, BinaryOperator<Integer>> OPERATION_MAP = new HashMap<>();

	    static {
	        OPERATION_MAP.put("+", (a, b) -> a + b);
	        OPERATION_MAP.put("-", (a, b) -> a - b);
	        OPERATION_MAP.put("*", (a, b) -> a * b);
	        OPERATION_MAP.put("/", (a, b) -> {
	            if (b != 0) {
	                return a / b;
	            } else {
	                throw new IllegalArgumentException("Division by zero is not allowed.");
	            }
	        });
	    }

	    @POST
	    @Path("calculate")
	    public Response addCalculation(calcu calcData) {
	        try {
	            int result = performOperation(calcData.getNumber1(), calcData.getNumber2(), calcData.getOperation());

	            calcu calculation = new calcu(calcData.getNumber1(), calcData.getNumber2(), calcData.getOperation(), result);
	            entityManager.persist(calculation);

	            return Response.status(Response.Status.OK).entity(calculation).build();
	        } catch (IllegalArgumentException e) {
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
	        }
	    }

	    @GET
	    @Path("calculations")
	    public Response fetchAllCalculations() {
	        try {
	            List<calcu> calculations = entityManager.createQuery("SELECT c FROM Calculation c", calcu.class).getResultList();
	            return Response.status(Response.Status.OK).entity(calculations).build();
	        } catch (Exception e) {
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
	        }
	    }

	    private int performOperation(int num1, int num2, String operation) {
	        BinaryOperator<Integer> operator = OPERATION_MAP.get(operation);
	        if (operator != null) {
	            return operator.apply(num1, num2);
	        } else {
	            throw new IllegalArgumentException("Invalid operation: " + operation);
	        }
	    }
	}


