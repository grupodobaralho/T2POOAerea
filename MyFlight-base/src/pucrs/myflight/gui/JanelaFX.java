package pucrs.myflight.gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import pucrs.myflight.modelo.Aeroporto;
import pucrs.myflight.modelo.Aeronave;
import pucrs.myflight.modelo.CiaAerea;
import pucrs.myflight.modelo.Geo;
import pucrs.myflight.modelo.GerenciadorAeronaves;
import pucrs.myflight.modelo.GerenciadorAeroportos;
import pucrs.myflight.modelo.GerenciadorCias;
import pucrs.myflight.modelo.GerenciadorPaises;
import pucrs.myflight.modelo.GerenciadorRotas;
import pucrs.myflight.modelo.Rota;
import pucrs.myflight.modelo.TreeOfRotas;

public class JanelaFX extends Application {

	final SwingNode mapkit = new SwingNode();
	private GerenciadorCias gerCias;
	private GerenciadorAeroportos gerAeroportos;
	private GerenciadorAeronaves gerAeronaves;
	private GerenciadorRotas gerRotas;
	private GerenciadorPaises gerPaises;
	private GerenciadorMapa gerenciador;	
	private EventosMouse mouse;
	private Aeroporto aeroSelecionado;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		setup();
		
		GeoPosition poa = new GeoPosition(-30.05, -51.18);
		gerenciador = new GerenciadorMapa(poa, GerenciadorMapa.FonteImagens.VirtualEarth);
		mouse = new EventosMouse();
		gerenciador.getMapKit().getMainMap().addMouseListener(mouse);
		gerenciador.getMapKit().getMainMap().addMouseMotionListener(mouse);

		createSwingContent(mapkit);
		
		BorderPane pane = new BorderPane();			
		GridPane leftPane = grid();		
		
		Label clearLB = new Label("Limpar tela");
		Button clearBT = new Button("Limpar");
		clearBT.setOnAction(e -> {
								  gerenciador.clear();
								  gerenciador.getMapKit().repaint();
							});
		
		Label exibeAerosLB = new Label("Exibir todos os aeroportos");
		Button exibeAerosBT = new Button("Exibir");
		exibeAerosBT.setOnAction(e -> {
			 gerenciador.clear(); 
			 exibeAeros();
			 gerenciador.getMapKit().repaint();
			 });
		
		Label aeroPaisLB = new Label("Exibir aeroportos do pa�s");
		Button aeroPaisBT = new Button("Exibir");
		aeroPaisBT.setOnAction(e -> {
			   gerenciador.clear(); 
			   consulta1();
			   gerenciador.getMapKit().repaint();
			   });
		
		Label distLB = new Label("Exibir rotas at� determinado raio");		
		Slider distSli = new Slider(0, 20_000, 0);
		
		distSli.setShowTickMarks(true);
		distSli.setShowTickLabels(true);
		distSli.setMajorTickUnit(5_000);		
		distSli.setBlockIncrement(1_000);
		distSli.setMinWidth(50);		
		
		Button distBT = new Button("Exibir");
		distBT.setOnAction(e-> { 
			gerenciador.clear(); 
			consulta2(distSli.getValue());
			gerenciador.getMapKit().repaint();
		  });
		
		Label rotasLigacoesLB = new Label("Exibir 3 liga��es a partir de um aeroporto de origem");
		Button rotasLigacoesBT = new Button("Exibir");
		rotasLigacoesBT.setOnAction(e -> {
			gerenciador.clear();										
			Aeroporto selecionado = gerAeroportos.buscarAeroProximo(gerenciador.getPosicao());
			Set<Aeroporto> origem = new HashSet<Aeroporto>();
			origem.add(selecionado);
			Set<Aeroporto> visitados = new HashSet<Aeroporto>();
			visitados.add(selecionado);
			consulta3(origem, visitados, 0);
			gerenciador.getMapKit().repaint();
			});
		
		Label rotasCiaLB = new Label("Exibir todos as rotas de uma Cia");
		Button rotasCiaBT = new Button("Exibir");
		ComboBox ciaSelect = new ComboBox();
		ciaSelect.getItems().addAll(gerCias.enviaAL());
		rotasCiaBT.setOnAction(e-> {
			gerenciador.clear();			
			consulta4(ciaSelect);			
			gerenciador.getMapKit().repaint();
			});
		
		Label caminhoLB = new Label("Buscar caminho entre 2 aeroportos");
		Button caminhoBT = new Button("Buscar");
		caminhoBT.setOnAction(e -> {
			gerenciador.clear();
			Set<Aeroporto> origem = new HashSet<Aeroporto>();
			origem.add(aeroSelecionado);
			Set<Aeroporto> visitados = new HashSet<Aeroporto>();
			visitados.add(aeroSelecionado);
			TreeOfRotas arvore = new TreeOfRotas(aeroSelecionado);
			consulta5(aeroSelecionado, gerAeroportos.buscarCod("POA"), origem, arvore);
			gerenciador.getMapKit().repaint();
			});
		
		GridPane geral = grid();
		geral.setHgap(50);
		geral.add(clearLB, 0, 0);
		geral.add(clearBT, 0, 1);
		geral.add(exibeAerosLB, 1, 0);
		geral.add(exibeAerosBT, 1, 1);
				
		//leftPane.setGridLinesVisible(true);
		leftPane.add(geral, 0, 0);
		leftPane.add(new Separator(), 0, 1);
		leftPane.add(aeroPaisLB, 0, 2);
		leftPane.add(aeroPaisBT, 0, 3);
		leftPane.add(new Separator(), 0, 4);
		leftPane.add(distLB, 0, 5);
		leftPane.add(distSli, 0, 6);		
		leftPane.add(distBT, 0, 7);
		leftPane.add(new Separator(), 0, 8);
		leftPane.add(rotasLigacoesLB, 0, 9);
		leftPane.add(rotasLigacoesBT, 0, 10);
		leftPane.add(new Separator(), 0, 11);
		leftPane.add(rotasCiaLB, 0, 12);		
		leftPane.add(ciaSelect, 0, 13);
		leftPane.add(rotasCiaBT, 0, 14);
		leftPane.add(new Separator(), 0, 15);
		leftPane.add(caminhoLB, 0, 16);
		leftPane.add(caminhoBT, 0, 17);
		
		pane.setCenter(mapkit);		
		pane.setLeft(leftPane);

		Scene scene = new Scene(pane, 500, 500);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Mapas com JavaFX");
		primaryStage.show();
	}
	
    private void setup() throws ClassNotFoundException, IOException {

    	gerPaises = new GerenciadorPaises();

		try{ 
			gerPaises.carregaDados();
		}
		catch (IOException e) {
		System.out.println("Imposs�vel ler countries.dat!");
		System.out.println("Msg: "+e);
		System.exit(1);
		}
				
		gerAeronaves = new GerenciadorAeronaves();
		try{
			gerAeronaves.carregaDados();
		}
		catch (IOException e){
			System.out.println("Imposs�vel ler equipment.dat!");
			System.out.println("Msg: "+e);
			System.exit(1);
		}
						
		gerAeroportos = new GerenciadorAeroportos();
		try{ 
			gerAeroportos.carregaDados(gerPaises.enviaHM());
		}
		catch (IOException | ClassNotFoundException e) {
		System.out.println("Imposs�vel ler airports.dat!");
		System.out.println("Msg: "+e);
		System.exit(1);
		}
		
		gerCias = new GerenciadorCias();
		try {
			gerCias.carregaDados();
		} 
		catch (IOException e) {
			System.out.println("Imposs�vel ler airlines.dat!");
			System.out.println("Msg: "+e);
			System.exit(1);
		}		
		
		gerRotas = new GerenciadorRotas();
		try {
			gerRotas.carregaDados(gerCias.enviaHM(), gerAeroportos.enviaHM(), gerAeronaves.enviaHM());
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Imposs�vel ler routes.dat!");
			System.out.println("Msg: "+e);
			System.exit(1);
		}		
	}
    
    
    public void exibeAeros() {   	
		gerenciador.clear();
		Set<MyWaypoint> pontos = new HashSet<>();
		List<Aeroporto> aeroportos = gerAeroportos.enviaAL();
		for(Aeroporto a : aeroportos)
			pontos.add(new MyWaypoint(Color.RED,a.getNome(), a.getLocal()));
		gerenciador.setPontos(pontos);
	}
    
    
    public void consulta1(){
    	gerenciador.clear();
    	Set<MyWaypoint> pontos = new HashSet<MyWaypoint>();    	
    	String codPais = aeroSelecionado.getPais().getCodigo();
    	Set<Aeroporto> lista = gerAeroportos.buscarPais(codPais);
    	for(Aeroporto a: lista)
    		pontos.add(new MyWaypoint(Color.BLUE, a.getNome(), a.getLocal()));
    	gerenciador.setPontos(pontos);    	
    }
    
    public void consulta2(double maxKm){    	
    	Tracado tr = new Tracado();    	
    	GeoPosition aeroSelecionadoPos = aeroSelecionado.getLocal();
    	Set<Rota> rotas = gerRotas.buscarOrigem(aeroSelecionado.getCodigo());
    	Set<MyWaypoint> pontos = new HashSet<MyWaypoint>();
    	pontos.add(new MyWaypoint(aeroSelecionadoPos));
    	for(Rota r: rotas){
    		if(Geo.distancia(aeroSelecionadoPos, r.getDestino().getLocal())<=maxKm){
            	GeoPosition aeroDestinoPos = r.getDestino().getLocal(); 
    			tr.addPonto(aeroSelecionadoPos);
            	tr.addPonto(aeroDestinoPos); 
            	gerenciador.addTracado(tr);
            	pontos.add(new MyWaypoint(aeroDestinoPos));
    		}
    	}
    	gerenciador.setPontos(pontos);    		
    }
    
    
    public void consulta3(Set<Aeroporto> origens, Set<Aeroporto> visitados, int ligacao){	
    	
    	if(ligacao<3){
    		
    		Set<Rota> rotas;
    		Set<Aeroporto> destinos = new HashSet<Aeroporto>();
    		
    		Tracado tr = new Tracado();
    		
    		if(ligacao==1)
    			tr.setCor(Color.ORANGE);
    		if(ligacao==2)
    			tr.setCor(Color.MAGENTA);
    		
    		for(Aeroporto a: origens){
    			rotas = gerRotas.buscarOrigem(a.getCodigo());
    			GeoPosition origem = a.getLocal();    			    	   	
    			rotas.stream()
    			.filter(r -> !visitados.contains(r.getDestino()))
    			.forEach(r -> {
    							tr.addPonto(origem); 
    							tr.addPonto(r.getDestino().getLocal()); 
    							gerenciador.addTracado(tr);    		
    							destinos.add(r.getDestino());    							
    							});     	   	
    		}
    		visitados.addAll(destinos);
    		ligacao++;
    		consulta3(destinos, visitados, ligacao);
    	}
    	else{
    		Set<MyWaypoint> pontos = new HashSet<MyWaypoint>();
    		for(Aeroporto a : visitados)
    			pontos.add(new MyWaypoint(a.getLocal()));
    		gerenciador.setPontos(pontos); 
     	}
    }
    
    public void consulta5(Aeroporto origem, Aeroporto destino, Set<Aeroporto> origens, TreeOfRotas arvore){
    	
    	
    	if(origens.size()>0){
    		
    		Set<Rota> rotas;
    		Set<Aeroporto> destinos = new HashSet<Aeroporto>();
    	
    		for(Aeroporto a: origens){    			
    			rotas = gerRotas.buscarOrigem(a.getCodigo());			
    			rotas.stream()
    			.filter(r -> !arvore.contains(r.getDestino()))
    			.forEach(r -> {
    							arvore.add(r.getDestino(), r.getOrigem());
    							destinos.add(r.getDestino());    							
    						  });
    		}
    		consulta5(origem, destino, destinos, arvore);
    	}
    		
    	else{
    		if(arvore.contains(destino)){    			
    			TreeOfRotas.Node aux = arvore.searchNodeRef(destino, arvore.getRoot());
    			ArrayList<Aeroporto> longWay = new ArrayList<Aeroporto>();
    			areWeThereYet(aux, longWay);
    		}
    	}
    }
	
    private void areWeThereYet(TreeOfRotas.Node aux, ArrayList<Aeroporto> way){
    	
    	way.add(aux.getElement());    	
    	if(aux.father!=null){
    		aux=aux.father;
    		areWeThereYet(aux,way);
    	}    	
    }
			
    	
		
    	
    	
    	
    	
    	
    
    
    public void consulta4(ComboBox ciaSelect){
    	Set<MyWaypoint> aeroportos = new HashSet<MyWaypoint>();
    	Tracado tr = new Tracado();    	
    	CiaAerea ciaSelecionada= (CiaAerea)ciaSelect.getValue();
    	String nomeCia = ciaSelecionada.getNome();
    	Set<Rota> rotas = gerRotas.buscarCia(ciaSelecionada.getCodigo());   	
       	rotas.stream()
        .forEach(r -> {             			
        				GeoPosition origem = r.getOrigem().getLocal();
        				GeoPosition destino = r.getDestino().getLocal();
        				aeroportos.add(new MyWaypoint(origem));
        				aeroportos.add(new MyWaypoint(destino));
               			tr.addPonto(origem);
               			tr.addPonto(destino);
               			gerenciador.addTracado(tr);               			
        			   });        
		gerenciador.setPontos(aeroportos);
		tableRotas(rotas, nomeCia);
	}
    
    
    private void tableRotas(Set<Rota> rotas, String cia){
		
		TableView<Rota> rotasCiaTB = new TableView<Rota>();
		TableColumn origemCol = new TableColumn("Origem");
        TableColumn destinoCol = new TableColumn("Destino");
        TableColumn aeronaveCol = new TableColumn("Aeronave");        
        TableColumn distanciaCol = new TableColumn("Dist�ncia (em km)");
        ObservableList<Rota> rotasCia = FXCollections.observableArrayList(rotas); 
		origemCol.setCellValueFactory(new PropertyValueFactory<Rota,Aeroporto>("Origem"));
		destinoCol.setCellValueFactory(new PropertyValueFactory<Rota,Aeroporto>("Destino"));
		aeronaveCol.setCellValueFactory(new PropertyValueFactory<Rota,Aeronave>("Aeronave"));
		distanciaCol.setCellValueFactory(new PropertyValueFactory<Rota,Double>("Distancia"));
		rotasCiaTB.setItems(rotasCia);
        rotasCiaTB.getColumns().addAll(origemCol,destinoCol,aeronaveCol,distanciaCol);
        
        ScrollPane scPane = new ScrollPane();
        scPane.setContent(rotasCiaTB);
        scPane.setFitToHeight(true);
        scPane.setFitToWidth(true);
        Scene scene = new Scene(scPane);
        Stage janela = new Stage();
        janela.setTitle("Rotas da companhia " + cia);
        janela.setScene(scene);
        janela.setResizable(true);
        janela.show();        
	}
   
    
    private Aeroporto aeroSelecionado(){
    	return aeroSelecionado = gerAeroportos.buscarAeroProximo(gerenciador.getPosicao());
    }
    
    private void createSwingContent(final SwingNode swingNode) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingNode.setContent(gerenciador.getMapKit());
			}
		});
	}
    

	private class EventosMouse extends MouseAdapter {
		private int lastButton = -1;

		@Override
		public void mousePressed(MouseEvent e) {
			JXMapViewer mapa = gerenciador.getMapKit().getMainMap();
			GeoPosition loc = mapa.convertPointToGeoPosition(e.getPoint());			
			lastButton = e.getButton();
			// Botão 3: seleciona localização
			if (lastButton == MouseEvent.BUTTON3) {
				gerenciador.setPosicao(loc);
				aeroSelecionado = aeroSelecionado();
				gerenciador.getMapKit().repaint();
			}
		}
	}
	
	private GridPane grid(){
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER_LEFT);
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setPadding(new Insets(10,5,10,5));				
		return pane;
	}

	public static void main(String[] args) {
		launch(args);			
	}
}
