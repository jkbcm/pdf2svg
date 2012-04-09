/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfviewer.PageWrapper;
import org.apache.pdfbox.pdfviewer.ReaderBottomPanel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ExtensionFileFilter;
import org.apache.pdfbox.util.ImageIOUtil;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/**
 * An application to read PDF documents.  This will provide Acrobat Reader like
 * funtionality.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDFReader extends javax.swing.JFrame
{
    private File currentDir=new File(".");
    private javax.swing.JMenuItem saveAsImageMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem nextPageItem;
    private javax.swing.JMenuItem previousPageItem;
    private JPanel documentPanel = new JPanel();
    private ReaderBottomPanel bottomStatusPanel = new ReaderBottomPanel();

    private PDDocument document = null;
    private List pages= null;
    
    private int currentPage = 0;
    private int numberOfPages = 0;
    private String currentFilename = null;
    
    /**
     * Constructor.
     */
    public PDFReader()
    {
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()
    {
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveAsImageMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        printMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        nextPageItem = new javax.swing.JMenuItem();
        previousPageItem = new javax.swing.JMenuItem();


        setTitle("PDFBox - PDF Reader");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitApplication();
            }
        });


        JScrollPane documentScroller = new JScrollPane();
        documentScroller.setViewportView( documentPanel );


        getContentPane().add( documentScroller, java.awt.BorderLayout.CENTER );
        getContentPane().add( bottomStatusPanel, java.awt.BorderLayout.SOUTH );

        fileMenu.setText("File");
        openMenuItem.setText("Open");
        openMenuItem.setToolTipText("Open PDF file");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        printMenuItem.setText( "Print" );
        printMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try
                {
                    if (document != null) 
                    {
                        document.print();
                    }
                }
                catch( PrinterException e )
                {
                    e.printStackTrace();
                }
            }
        });
        fileMenu.add( printMenuItem );

        saveAsImageMenuItem.setText( "Save as image" );
        saveAsImageMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                if (document != null) 
                {
                    saveImage();
                }
            }
        });
        fileMenu.add( saveAsImageMenuItem );

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitApplication();
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        viewMenu.setText("View");
        nextPageItem.setText("Next page");
        nextPageItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        nextPageItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                nextPage();
            }
        });
        viewMenu.add(nextPageItem);

        previousPageItem.setText("Previous page");
        previousPageItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        previousPageItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                previousPage();
            }
        });
        viewMenu.add(previousPageItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);


        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-700)/2, (screenSize.height-600)/2, 700, 600);
    }


    private void updateTitle() 
    {
        setTitle( "PDFBox - " + currentFilename + " ("+(currentPage+1)+"/"+numberOfPages+")");
    }
    
    private void nextPage()
    {
        if (currentPage < numberOfPages-1) 
        {
            currentPage++;
            updateTitle();
            showPage(currentPage);
        }
    }
    
    private void previousPage()
    {
        if (currentPage > 0 ) 
        {
            currentPage--;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDir);

        ExtensionFileFilter pdfFilter = new ExtensionFileFilter(new String[] {"PDF"}, "PDF Files");
        chooser.setFileFilter(pdfFilter);
        int result = chooser.showOpenDialog(PDFReader.this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            String name = chooser.getSelectedFile().getPath();
            currentDir = new File(name).getParentFile();
            try
            {
                openPDFFile(name);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void exitApplication()
    {
        try
        {
            if( document != null )
            {
                document.close();
            }
        }
        catch( IOException io )
        {
            //do nothing because we are closing the application
        }
        this.setVisible( false );
        this.dispose();
    }

    /**
     * @param args the command line arguments
     *
     * @throws Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception
    {
        PDFReader viewer = new PDFReader();
        if( args.length >0 )
        {
            viewer.openPDFFile( args[0] );
        }
        viewer.setVisible(true);
    }

    private void openPDFFile(String file) throws Exception
    {
        if( document != null )
        {
            document.close();
            documentPanel.removeAll();
        }
        InputStream input = null;
        File f = new File( file );
        input = new FileInputStream(f);
        document = parseDocument( input );
        pages = document.getDocumentCatalog().getAllPages();
        numberOfPages = pages.size();
        currentFilename = f.getAbsolutePath();
        currentPage = 0;
        updateTitle();
        showPage(0);
    }
    
    private void showPage(int pageNumber)
    {
        try 
        {
            PageWrapper wrapper = new PageWrapper( this );
            wrapper.displayPage( (PDPage)pages.get(pageNumber) );
            if (documentPanel.getComponentCount() > 0)
            {
                documentPanel.remove(0);
            }
            documentPanel.add( wrapper.getPanel() );
            pack();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    private void saveImage()
    {
        try 
        {
            PDPage pageToSave = (PDPage)pages.get(currentPage);
            BufferedImage pageAsImage = pageToSave.convertToImage();
            String imageFilename = currentFilename;
            if (imageFilename.toLowerCase().endsWith(".pdf"))
            {
                imageFilename = imageFilename.substring(0, imageFilename.length()-4);
            }
            imageFilename += "_" + (currentPage + 1); 
            ImageIOUtil.writeImage(pageAsImage, "png", imageFilename,  BufferedImage.TYPE_USHORT_565_RGB, 300);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }
    /**
     * This will parse a document.
     *
     * @param input The input stream for the document.
     *
     * @return The document.
     *
     * @throws IOException If there is an error parsing the document.
     */
    private static PDDocument parseDocument( InputStream input )throws IOException
    {
        PDDocument document = PDDocument.load( input );
        if( document.isEncrypted() )
        {
            try
            {
                document.decrypt( "" );
            }
            catch( InvalidPasswordException e )
            {
                System.err.println( "Error: The document is encrypted." );
            }
            catch( org.apache.pdfbox.exceptions.CryptographyException e )
            {
                e.printStackTrace();
            }
        }

        return document;
    }

    /**
     * Get the bottom status panel.
     *
     * @return The bottom status panel.
     */
    public ReaderBottomPanel getBottomStatusPanel()
    {
        return bottomStatusPanel;
    }
}
