package pucrs.myflight.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GerenciadorRotas {

	private ArrayList<Rota> rotasAL;
	
	
	public GerenciadorRotas() throws ClassNotFoundException, IOException{
		rotasAL = new ArrayList<Rota>();		
	}			
	
	public void carregaDados(HashMap<String, CiaAerea> empresasHM, HashMap<String, Aeroporto> aeroportosHM, HashMap<String, Aeronave> aeronavesHM) throws IOException, ClassNotFoundException{
			
		Path arq = Paths.get("routes.dat");
		try(BufferedReader br = Files.newBufferedReader(arq, Charset.forName("utf8"))){
		String linha = br.readLine();
		linha = br.readLine();		
		while((linha = br.readLine())!=null){	
			Rota aux;
			Scanner scan1 = new Scanner(linha).useDelimiter(";");
			String cia = scan1.next();
			String origem = scan1.next();
			String destino = scan1.next();
			String codeshare = scan1.next();
			String paradas = scan1.next();
			//if(scan1.hasNext()){
				String equip = scan1.next();
				Scanner scan2 = new Scanner(equip);
				equip = scan2.next();
				aux = new Rota(empresasHM.get(cia), aeroportosHM.get(origem), aeroportosHM.get(destino), aeronavesHM.get(equip));
			/*}
			else
				System.out.println("Rota sem aeronave:" + cia + " " + origem + " " + destino);
				aux = new Rota(empresasHM.get(cia), aeroportosHM.get(origem), aeroportosHM.get(destino));*/
			rotasAL.add(aux);
		}
		}
	}
	
	public ArrayList<Rota> buscarCia(String cia){
		List<Rota> rotaCia = rotasAL.stream()
			.filter(r -> r.getCia().getCodigo().equals(cia))
			.collect(Collectors.toList());
		for(Rota r: rotaCia)
		System.out.println(r);
			return (ArrayList)rotaCia;
	}
	
}
		
			
			
						
			