package edu.umn.cs.crisys.tb.codegen.common.emitters.Port.RPC;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import edu.umn.cs.crisys.tb.TbException;
import edu.umn.cs.crisys.tb.codegen.VxWorks.VxWorksUtil;
import edu.umn.cs.crisys.tb.codegen.common.emitters.EmitterFactory;
import edu.umn.cs.crisys.tb.codegen.common.emitters.Port.PortConnectionEmitter;
import edu.umn.cs.crisys.tb.codegen.common.emitters.Port.PortEmitterCamkes;
import edu.umn.cs.crisys.tb.codegen.common.emitters.Port.PortEmitterEChronos;
import edu.umn.cs.crisys.tb.codegen.common.emitters.Port.PortEmitterLinux;
import edu.umn.cs.crisys.tb.codegen.common.emitters.Port.PortEmitterVxWorks;
import edu.umn.cs.crisys.tb.codegen.common.names.ExternalHandlerNames;
import edu.umn.cs.crisys.tb.codegen.common.names.ModelNames;
import edu.umn.cs.crisys.tb.codegen.common.names.ThreadImplementationNames;
import edu.umn.cs.crisys.tb.codegen.common.names.TypeNames;
import edu.umn.cs.crisys.tb.codegen.eChronos.EChronosUtil;
import edu.umn.cs.crisys.tb.codegen.linux.LinuxUtil;
import edu.umn.cs.crisys.tb.model.OSModel;
import edu.umn.cs.crisys.tb.model.connection.PortConnection;
import edu.umn.cs.crisys.tb.model.port.DispatchableInputPort;
import edu.umn.cs.crisys.tb.model.port.ExternalHandler;
import edu.umn.cs.crisys.tb.model.port.InputDataPort;
import edu.umn.cs.crisys.tb.model.port.InputEventPort;
import edu.umn.cs.crisys.tb.model.port.InputPort;
import edu.umn.cs.crisys.tb.model.port.OutputDataPort;
import edu.umn.cs.crisys.tb.model.port.OutputEventPort;
import edu.umn.cs.crisys.tb.model.port.OutputPort;
import edu.umn.cs.crisys.tb.model.port.PortFeature;
import edu.umn.cs.crisys.tb.model.type.BoolType;
import edu.umn.cs.crisys.tb.model.type.IntType;
import edu.umn.cs.crisys.tb.model.type.Type;
import edu.umn.cs.crisys.tb.model.type.UnitType;
import edu.umn.cs.crisys.tb.util.Util;

public class PortEmitterRPCAllEvent implements PortEmitterCamkes, PortEmitterEChronos, PortEmitterVxWorks, PortEmitterLinux, PortEmitterRPC {

   public static boolean isApplicable(PortFeature pf) {
      // right kind of port
      boolean ok = (pf instanceof InputEventPort ||
                    pf instanceof OutputEventPort);
      
      OSModel model = Util.getElementOSModel(pf);
      OSModel.OSTarget target = model.getOsTarget();

      // linux is not yet supported until we get our act together.
      ok &= (target == OSModel.OSTarget.CAmkES || 
             target == OSModel.OSTarget.eChronos ||
             target == OSModel.OSTarget.VxWorks || 
             target == OSModel.OSTarget.linux);
      
      if (target == OSModel.OSTarget.linux) {
         // TODO: check here to see if source and target process 
         // are the same for all connections from this port.
         ok &= Util.allConnectionsInProcess(pf);
      }
      
      return ok;
   }
   
   private PortFeature eventDataPort;
   private OSModel model; 
   Type indexType = new IntType(32, false); 
   
   public PortEmitterRPCAllEvent(PortFeature pf) {
      this.eventDataPort = pf;
      this.model = Util.getElementOSModel(pf);
   }
   
   @Override
   public void addPortPublicTypeDeclarations(Map<String, Type> typeList) {
      // no-op for the moment; I could add support for 
      // auto-declaration of types, but right now this is already 
      // handled.
   }

   @Override
   public PortFeature getModelElement() {
      return this.eventDataPort;
   }

   @Override
   public void getWritePortCFiles(File directory) {
      // no-op for RPCEventDataPorts
   }

   @Override
   public void getWritePortHFiles(File directory) {
      // no-op for RPCEventDataPorts
   }
   
   public ST getTemplateST(String stName) {
      STGroupFile template = Util.createTemplate("PortEmitterRPCAllEvent.stg");
      return template.getInstanceOf(stName); 
   }
   @Override
   public String getWritePortHPrototypes() {
      String result = ""; 
      
      ST st; 
      PortFeature p = getModelElement();
      if (p instanceof OutputEventPort) {
         st = getTemplateST("writePortWriterPrototype");         
         st.add("port", this);
         result += st.render();
      } else if (p instanceof InputEventPort) {
         st = getTemplateST("writePortReaderPrototype");         
         st.add("port", this);
         result += st.render();
         
         st = getTemplateST("writeUdePrototype");
         st.add("dispatcher", this);
         result += st.render();
      } else {
         throw new TbException("Error: writePortHPrototypes: port " + this.getName() + " is not an event data port.");
      }
      
      result += writeOptPortThreadInitializerPrototype("void"); 
      return result;
   }


   @Override
   public String getWritePortDeclarations() {
      ST st; 
      PortFeature p = getModelElement();
      if (p instanceof OutputEventPort) {
         st = getTemplateST("componentRemoteWriterDecl");   
      } else if (p instanceof InputEventPort) {
         if (this.getHasData()) {
            st = getTemplateST("InputEventDataPortDecls");
         } else {
            st = getTemplateST("InputEventPortDecls");
         }
      } else {
         throw new TbException("Error: writePortDeclarations: port " + this.getName() + " is not an event data port.");
      }
      st.add("port", this);
      return st.render();
   }

   @Override
   public String getWritePortPreEntrypoint() {
      // nothing to do for "active" threads; for the moment
      // I'm not going to support "passive" threads.
      return "";
   }

   @Override
   public String getWritePortPostEntrypoint() {
      // nothing to do for "active" threads; for the moment
      // I'm not going to support "passive" threads.
      // However, this should make it easy!!
      return "";
   }

   @Override
   public String getWritePortEventResponder() {
      ST st; 
      PortFeature p = getModelElement();
      if (p instanceof InputEventPort && this.getHasDispatcher()) {
         if (p.hasData()) {
            st = getTemplateST("eventDataDispatcher");
         } else {
            st = getTemplateST("eventDispatcher");
         }
         st.add("dispatcher", EmitterFactory.port(p));
         return st.render(); 
      } else {
         return "";
      }
   }

   public String writeOptPortThreadInitializerPrototype(String v) {
      String result = ""; 
      ExternalHandler eh = this.getModelElement().getInitializeEntrypointSourceText(); 
      if (eh != null) {
         result += eh.getHandlerName() + "(" + v + ");\n";
      }
      return result;
   }

   
   @Override
   public String getWritePortThreadInitializer() {
      return writeOptPortThreadInitializerPrototype(""); 
   }

   @Override
   public String getName() {
      return this.getModelElement().getName();
   }

   @Override
   public String getQualifiedName() {
      return this.getModelElement().getQualifiedName();
   }


   /************************************************************
    * 
    * VxWorks-specific functions 
    * 
    ************************************************************/

   
   @Override
   public String getVxWorksAddCommonHFileDeclarations() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += VxWorksUtil.writeExternMutexDecl(this.getMutex());
         toReturn += getEChronosAddCommonHFileDeclarations();
      } 
      return toReturn;
   }

   @Override
   public String getVxWorksAddMainCFileIncludes() {
      return "";
   }

   @Override
   public String getVxWorksAddMainCFileDeclarations() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += VxWorksUtil.writeMutexDecl(this.getMutex());
      }
      return toReturn;
   }

   @Override
   public String getVxWorksAddMainCFileInitializers() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += this.getMutex() + " = " + VxWorksUtil.createMutex() + ";\n";
         toReturn += "assert(" + this.getMutex() + " != NULL );\n";
      }
      ExternalHandler initializer = 
            this.getModelElement().getInitializeEntrypointSourceText();
      if (initializer != null) {
         toReturn += initializer.getHandlerName() + "();\n ";
      }

      return toReturn;
   }

   @Override
   public String getVxWorksAddMainCFileDestructors() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += "semDelete(" + this.getMutex() + ");\n";
      }
      return toReturn;
   }

   /************************************************************
    * 
    * linux-specific functions (implementing RPCEventDataPortCamkes)
    * 
    ************************************************************/
   @Override
   public String getLinuxAddCommonHFileDeclarations() {
      // TODO Auto-generated method stub
         String toReturn = "\n"; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += LinuxUtil.writeExternMutexDecl(this.getMutex());
         toReturn += getEChronosAddCommonHFileDeclarations();
      } 
      return toReturn + "\n";
   }

   @Override
   public String getLinuxAddMainCFileIncludes() {
      return "";
   }

   @Override
   public String getLinuxAddMainCFileDeclarations() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += LinuxUtil.writeMutexDecl(this.getMutex());
      }
      return toReturn;
   }

   @Override
   public String getLinuxAddMainCFileInitializers() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += LinuxUtil.createInterprocMutex(this.getMutex());
      }
      ExternalHandler initializer = 
            this.getModelElement().getInitializeEntrypointSourceText();
      if (initializer != null) {
         toReturn += initializer.getHandlerName() + "();\n ";
      }
      return toReturn;
   }

   @Override
   public String getLinuxAddMainCFileDestructors() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += LinuxUtil.deleteMutex(this.getMutex());
      }
      return toReturn;
   }

   /************************************************************
    * 
    * eChronos-specific functions (implementing RPCEventDataPortCamkes)
    * 
    ************************************************************/

   public String getEChronosAddPrxMutexes() {
      String toReturn = "";
      if (this.getModelElement() instanceof InputPort) {
         toReturn += EChronosUtil.addPrxMutex(this.getMutex());
      }
      return toReturn;
   }
   
   
   
   public String getEChronosAddPrxSemaphores() {
      return "";
   }

   @Override
   public String getEChronosAddCommonHFileDeclarations() {
      String toReturn = ""; 
      if (this.getModelElement() instanceof InputPort) {
         toReturn += EChronosUtil.eChronosPortWriterPrototype(
            getIncomingWriterName(), this.getModelElement().getType()); 
      }
      return toReturn;
   }

   @Override
   public String getEChronosAddTrampolines() { return ""; }
   
   @Override
   public String getEChronosAddInternalIrqs() { return ""; }
   
   @Override
   public String getEChronosAddExternalIrqs() { return ""; }

   
   /************************************************************
    * 
    * CAmkES-specific functions (implementing RPCDataPortCamkes)
    * 
    ************************************************************/

   private String addComponentInputPortDeclarations() {
      String element = 
         "provides " + this.getType().getReaderWriterInterfaceName() + " " + 
               this.getName() + ";\n" + 
         "has mutex " + this.getMutex() + ";\n";
      return element;
   }
   
   private String addComponentOutputDataDeclarations() {
      String element = 
         "uses " + this.getType().getReaderWriterInterfaceName() + " " + 
               this.getQualifiedName() + ";\n"; 
      return element; 
   }
   
   @Override
   public String getCamkesAddComponentPortLevelDeclarations() {
      // TODO Auto-generated method stub
      PortFeature pf = this.getModelElement();
      if (pf instanceof InputPort) {
         return addComponentInputPortDeclarations();
      } else if (pf instanceof OutputPort) {
         return addComponentOutputDataDeclarations();
      } else {
         throw new TbException("RPCEventDataPortEmitter::addComponentPortLevelDeclarations: event data port emitter used with non-event data port class: " + pf.getName());
      }
   }

   public String addAssemblyConnection(PortConnection conn, OSModel model) {
      ST st = getTemplateST("connectReaderWriter"); 
      st.add("connection", EmitterFactory.portConnection(conn));
      st.add("model", EmitterFactory.model(model));
      return st.render();
   }
  
   @Override
   public String getCamkesAddAssemblyFileCompositionPortDeclarations() {
      PortFeature pf = this.getModelElement();
      if (pf instanceof OutputPort) {
         String result = ""; 
         for (PortConnection conn : pf.getConnections()) {
            result += addAssemblyConnection(conn, this.model); 
         }
         return result;         
      } else {
         return "";
      }
   }
   
   @Override
   public String getCamkesAddAssemblyFileConfigDeclarations() {
      return "";
   }
   
   @Override
   public String getCamkesAddAssemblyFilePortDeclarations() { return ""; }
   
   /************************************************************
    * 
    * Names functions used by the .stg templates for input queues
    *
    * queueDataDecl
    * queueFullDecl
    * queueFrontDecl
    * queueBackDecl
    * queueIsFullName
    * queueFrontName
    * queueBackName
    * queueFullName
    * queueIsEmptyName
    * queueReadName
    * type
    * name
    * index
    * indexMax
    * queueSize
    * queueWriteName
    * incomingPortWriterName <--> incomingWriterName --> OS-specific
    * portLockStmt <--> lockStmt                     --> OS-specific
    * portUnlockStmt <--> unlockStmt                 --> OS-specific
    * aadlReaderFnName <--> getLocalReaderName       
    * hasDispatcher
    * dispatcherMainLockReleaseStmt                  --> OS-specific 
    * 
    * ...and for output queues
    * outgoingWriterName
    * 
    * ...and for event dispatchers
    * varDecl
    * userEntrypointCallerPrototype
    * activeThreadInternalDispatcherFnName
    * incomingUserEntrypointCallerName <--> userEntrypointCallerName --> OS-specific
    * 
    * connections
    * 
    * the entrypointCallerName is only dispatched remotely for 
    *    passive threads.  For active threads it will always be 
    *    dispatched from the main thread dispatch loop.
    * 
    * For now, activeThreadInternalDispatcherFnName is just the 
    * name of the entrypointCallerName; once (if) we add back in 
    * passive threads, we will adjust the name and emit more declarations.
    * 
    ************************************************************/

   public String getPrefix() { return Util.getPrefix(); }
   
   public String getQueueFrontName() { return getPrefix() + "_queue_front_" + this.getName(); }
   public String getQueueBackName()  { return getPrefix() + "_queue_back_" + this.getName(); }
   public String getQueueDataName()  { return getPrefix() + "_queue_" + this.getName(); }
   public String getQueueFullName() { return getPrefix() + "_queue_full_" + this.getName(); }
   public String getQueueIsFullName() { return getPrefix() + "_queue_is_full_" + this.getName(); }
   public String getQueueIsEmptyName() { return getPrefix() + "_queue_is_empty_" + this.getName(); }
   public String getQueueReadName() { return getPrefix() + "_queue_read_" + this.getName(); }
   public String getQueueWriteName() { return getPrefix() + "_queue_write_" + this.getName(); }

   public String getQueueDataDecl()  { 
      if (!(this.getModelElement() instanceof InputEventPort)) {
         throw new TbException("Error: getQueueDecl: port " + this.getModelElement().getName() + " is not an input event port so has no queue.");
      }
      InputEventPort iep = (InputEventPort)this.getModelElement(); 
      return iep.getQueueType().getCType().varString(getQueueDataName());
   }

   public boolean getHasData() {
      return (this.getModelElement().getType() instanceof UnitType); 
   }
   
   public String getQueueFrontDecl() {
      return indexType.getCType().varString(getQueueFrontName());
   }

   public String getQueueBackDecl() {
      return indexType.getCType().varString(getQueueBackName()); 
   }

   public String getQueueFullDecl() {
      return (new BoolType()).getCType().varString(getQueueFullName()); 
   }

   public TypeNames getType() { 
      return EmitterFactory.type(this.getModelElement().getType()); 
   }

   public TypeNames getIndexType() {
      return EmitterFactory.type(this.indexType); 
   }

   public String getIndex() {
      return getName() + "_index";
   }

   // relevant (kinda) to event ports
   // we should never enqueue more than 32k messages!
   public String getIndexMax() {
      return "32767";
   }
   
   // relevant to only event data ports
   public String getQueueSize() {
      PortFeature dp = this.getModelElement();
      if (!(dp instanceof InputEventPort)) {
         throw new TbException("Error: getQueueSize: port " + dp.getName() + " is not an input event port so has no queue.");
      }
      InputEventPort iep = (InputEventPort)dp; 
      return Integer.toString(iep.getQueueSize());
   }
   
   public String writeType() {
      return "_write" + getModelElement().getType().getCType().typeString();
   }

   public String getLpcPortWriterName() {
      return Util.getPrefix_() + getModelElement().getQualifiedName() + 
            writeType();
   }

   // middleware functions; these must be compatible with the OS.
   @Override
   public String getIncomingWriterName() {
      PortFeature dp = getModelElement();
      if (model.getOsTarget() == OSModel.OSTarget.CAmkES) {
         return dp.getName() + writeType() ;
      } else if (model.getOsTarget() == OSModel.OSTarget.eChronos ||
            model.getOsTarget() == OSModel.OSTarget.VxWorks ||
            model.getOsTarget() == OSModel.OSTarget.linux) {
         return getLpcPortWriterName();
      } else {
         throw new TbException("Error: getIncomingPortWriterName: OS " + model.getOsTarget() + " is not a known OS target.");
      }
   }

   // local reader/writer name does not have to be compatible with any CAmkES stuff.
   public String getLocalReaderWriterName(String readWrite) {
      PortFeature dp = getModelElement();
      if (dp.getCommprimFnNameOpt() != null) {
         return dp.getCommprimFnNameOpt();
      } else {
         String result = Util.getPrefix_() +
            dp.getOwner().getNormalizedName() + "_" + readWrite + "_" + dp.getName();
         return result;
      }
   }   
   
   public String getLocalReaderName() {
      return getLocalReaderWriterName("read");
   }
   
   public String getLocalWriterName() {
      return getLocalReaderWriterName("write");
   }   

   
   public String getMutex() {
      return (Util.getPrefix_() + getModelElement().getOwner().getNormalizedName() + "_" + getModelElement().getName() + "_mut").toLowerCase();
    }
    
    public String getEChronosMutexConst() {
       return (ModelNames.getEChronosPrefix() + "_MUTEX_ID_" + getMutex()).toUpperCase();
    }
   
   // getOutgoingWriterName()
   public String getLockStmt() {
      if (model.getOsTarget() == OSModel.OSTarget.CAmkES) {
         return getMutex() + "_lock();" ;
      } else if (model.getOsTarget() == OSModel.OSTarget.eChronos) {
         return "rtos_mutex_lock(" + getEChronosMutexConst() + ");";
      } else if (model.getOsTarget() == OSModel.OSTarget.VxWorks) {
         return "semTake(" + getMutex() + ", WAIT_FOREVER);";
      } else if (model.getOsTarget() == OSModel.OSTarget.linux) {
         return LinuxUtil.waitSem(getMutex());
      } else {
         throw new TbException("Error: getLockStmt: OS " + model.getOsTarget() + " is not a known OS target.");
      }
   }

   public String getUnlockStmt() {
      if (model.getOsTarget() == OSModel.OSTarget.CAmkES) {
         return getMutex() + "_unlock();" ;
      } else if (model.getOsTarget() == OSModel.OSTarget.eChronos) {
         return "rtos_mutex_unlock(" + getEChronosMutexConst() + ");";
      } else if (model.getOsTarget() == OSModel.OSTarget.VxWorks) {
         return "semGive(" + getMutex() + ");";
      } else if (model.getOsTarget() == OSModel.OSTarget.linux) {
         return LinuxUtil.postSem(getMutex());
      } else {
         throw new TbException("Error: getunlockStmt: OS " + model.getOsTarget() + " is not a known OS target.");
      }
   }
   
   // relevant only for event-data ports
   public boolean getHasDispatcher() {
      if (this.getModelElement() instanceof DispatchableInputPort) {
         return  ((DispatchableInputPort)this.getModelElement()).getExternalHandlerList().size() != 0;
      }
      else return false;
   }

   public String getDispatcherMainLockReleaseStmt() {
      return (EmitterFactory.threadImplementation(
               this.getModelElement().getOwner())).
                  getDispatcherMainLockReleaseStmt();
   }
      
   public String getVarDecl() {
      return this.getModelElement().getType().getCType().varString(getName());
   }

   /*
   public String getIdlDispatcherName() {
      return "dispatch_" + getName();
   }
   */

   public String getUserEntrypointCallerName() {
      return this.getPrefix() + "_entrypoint_" + 
            this.getQualifiedName(); 
/*
      if (this.model.getOsTarget() == OSModel.OSTarget.CAmkES){
         ThreadImplementationNames tnames = 
               EmitterFactory.threadImplementation(this.getModelElement().getOwner());
         String name = tnames.getComponentDispatcherInterfaceVarIdName() + "_" + 
                     this.getIdlDispatcherName() ; 
         return name;
      } else if (this.model.getOsTarget() == OSModel.OSTarget.eChronos) {
         return this.getPrefix() + "_entrypoint_" + 
                this.getQualifiedName(); 
      } else if (this.model.getOsTarget() == OSModel.OSTarget.VxWorks) {
         return this.getIncomingWriterName();
      } else {
         throw new TbException("in RPCEventDataPortEmitter::getUserEntrypointName: OS [" + 
               this.model.getOsTarget() + "] is not supported.");
      }
  */
   }
   
   public String getActiveThreadInternalDispatcherFnName() {
      return getUserEntrypointCallerName(); 
   }
   
   public String getUserEntrypointCallerPrototype() {
      String result = ""; 
      result += "void " + getUserEntrypointCallerName() + 
            "(const " + getType().getCamkesInputType().getName() + " in_arg)";
      return result;
   }

   public List<ExternalHandlerNames> getExternalHandlers() {
      DispatchableInputPort dip = ((DispatchableInputPort) this.eventDataPort);
      List<ExternalHandlerNames> ehl = new ArrayList<>(); 
      for (ExternalHandler eh : dip.getExternalHandlerList()) {
         ehl.add(EmitterFactory.externalHandler(eh)); 
      }
      return ehl;
   }

   public List<PortConnectionEmitter> getConnections() {
      List<PortConnectionEmitter> pcl = new ArrayList<>(); 
      for (PortConnection pc: this.getModelElement().getConnections()) {
         pcl.add(EmitterFactory.portConnection(pc));
      }
      return pcl;
   }
   
}
