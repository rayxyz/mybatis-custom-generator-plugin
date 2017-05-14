package com.raywang.mybatis.generator.plugins;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class CustomGeneratorPlugin extends PluginAdapter {

    private String modelPackage;
    private String appobjPath;
    private String appobjPackage;
    private String appobjSuffix;
    private String baseMapper;
    private String baseMapperName;
    private String pkType;
    
    private String currentModelPackagePath;
    private String currentAppobjPackagePath;

    public boolean validate(List<String> arg0) {
        this.modelPackage = properties.getProperty("modelPackage");
        this.appobjPath = new File("").getAbsoluteFile().getAbsolutePath() + "\\" + properties.getProperty("appobjPath");
        this.appobjPackage = properties.getProperty("appobjPackage");
        this.appobjSuffix = properties.getProperty("appobjSuffix");
        this.baseMapper = properties.getProperty("baseMapper");
        this.baseMapperName = properties.getProperty("baseMapperName");
        this.pkType = properties.getProperty("pkType");

        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        // Generate AOS
        String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        String appobjName = modelName + appobjSuffix;
        this.currentModelPackagePath = this.modelPackage + "." + modelName;
        this.currentAppobjPackagePath = this.appobjPackage + "." + appobjName;
        String appobjImpPath =
                new StringBuilder().append(appobjPackage).append(".").append(appobjName).toString();

        this.createApplicationObjects(appobjName, modelName); // Generate appobjs

        // System.out.println(
        // "introspectedTable.getBaseRecordType(): " + introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(this.baseMapperName + "<" + appobjName + ","
                + this.pkType + "," + introspectedTable.getExampleType() + ">");
        FullyQualifiedJavaType impBaseMapper = new FullyQualifiedJavaType(this.baseMapper);
        FullyQualifiedJavaType impAppobj = new FullyQualifiedJavaType(appobjImpPath);

        interfaze.addSuperInterface(fqjt);
        interfaze.addImportedType(impBaseMapper);

        interfaze.addImportedType(impAppobj);

        interfaze.getMethods().clear();
        interfaze.getAnnotations().clear();

        return true;
    }

    private void createApplicationObjects(String appobjName, String modelName) {
        String filePath = this.appobjPath + "\\" + appobjName + ".java";
        System.out.println("Generating " + filePath);
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("File " + filePath + " exists.");
        } else {
            generateAppobjJavaFile(appobjName, modelName);
        }
    }

    private void generateAppobjJavaFile(String appobjName, String modelName) {
        try {
            String filePath = this.appobjPath + "\\" + appobjName + ".java";
            File sourceFile = new File(filePath);
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(
                    "package " + this.appobjPackage + ";" + 
                    "\n\n" + 
                    "import " + this.currentModelPackagePath + ";" +
                    "\n\n" + 
                    "public class " + appobjName + " extends " + modelName + " {" + 
                    "\n\n" + 
            "}");
            writer.close();
            System.out.println("File " + filePath + " generated.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
//    addResultMapWithoutBLOBsElement(answer);
//    addResultMapWithBLOBsElement(answer);
    
    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.changeBaseResultMapType(element);
        return true;
    }
    
    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.changeBaseResultMapType(element);
        return true;
    }
    
    private void changeBaseResultMapType(XmlElement element) {
        boolean caughtit = false;
        Attribute attrCaught = null;
        List<Attribute> attributes = element.getAttributes();
        for (Attribute attr : attributes) {
            if (caughtit) break;
            if ("id".equals(attr.getName()) && "BaseResultMap".equals(attr.getValue())) {
                for (Attribute attrx : attributes) {
                    if (attrx.getName().equals("type")) {
                        attrCaught = attrx;
                        caughtit = true;
                        break;
                    }
                }
            }
        }
        if (caughtit && attrCaught != null) {
            attributes.remove(attrCaught);
            element.addAttribute(new Attribute("type", this.currentAppobjPackagePath));
        }
    }

}
