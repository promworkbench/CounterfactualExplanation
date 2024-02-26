package org.processmining.CounterfactualRecommendation.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.processmining.framework.util.ui.widgets.ProMScrollPane;
import org.processmining.framework.util.ui.widgets.WidgetColors;

@SuppressWarnings("serial")
public class CheckBoxList extends JList<JCheckBox> {
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public CheckBoxList() {
    setCellRenderer(new CellRenderer());
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
          JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
          checkbox.setSelected(!checkbox.isSelected());
          repaint();
        }
      }
    });
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  public CheckBoxList(ListModel<JCheckBox> model){
    this();
    setModel(model);
  }
  //TODO private void setup(final String title) {
  private void setup(final String title, JList jList) {
		jList.setBackground(WidgetColors.COLOR_LIST_BG);
		jList.setForeground(WidgetColors.COLOR_LIST_FG);
		jList.setSelectionBackground(WidgetColors.COLOR_LIST_SELECTION_BG);
		jList.setSelectionForeground(WidgetColors.COLOR_LIST_SELECTION_FG);

		final ProMScrollPane scroller = new ProMScrollPane(jList);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		final JLabel providersLabel = new JLabel(title);
		providersLabel.setOpaque(false);
		providersLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(providersLabel);
		add(Box.createVerticalStrut(8));
		add(scroller);
		setMinimumSize(new Dimension(200, 100));
		setMaximumSize(new Dimension(1000, 1000));
		setPreferredSize(new Dimension(1000, 200));
	}


  protected class CellRenderer implements ListCellRenderer<JCheckBox> {
    public Component getListCellRendererComponent(
        JList<? extends JCheckBox> list, JCheckBox value, int index,
        boolean isSelected, boolean cellHasFocus) {
      JCheckBox checkbox = value;

      //Drawing checkbox, change the appearance here
      checkbox.setBackground(isSelected ? getSelectionBackground()
          : getBackground());
      checkbox.setForeground(isSelected ? getSelectionForeground()
          : getForeground());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ? UIManager
          .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      return checkbox;
    }
  }
}